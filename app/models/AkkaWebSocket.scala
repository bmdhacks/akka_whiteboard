package com.bmdhacks.akkawebsocket

import akka.actor._
import akka.util.duration._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current

// These objects are used to structure message data sent over Akka
case object Quit
case object NewWebSocket
case class WebSocketResponse(in: Iteratee[JsValue,_], out: Enumerator[JsValue])

/**
 * This is a very simple Actor that bridges between Akka and a Play websocket connection.
 * Play uses an Iteratee/Enumerator pair to communicate with the websocket, and
 * this class just reads Akka messages into the Enumerator, and iterates through
 * the Iteratee sending the contents as Akka messages.  All messages are sent to
 * the parent Actor, which should contain the real application logic
 */
class WebSocketActor extends Actor {

  // Items placed in this Enumerator get sent to the browser via the websocket
  val out = Enumerator.imperative[JsValue]()

  // this handles any messages sent from the browser to the server over the socket
  val in = Iteratee.foreach[JsValue] { message =>
	// just take the socket data and send it as an akka message to our parent
	context.parent ! message
  }.mapDone { _ =>
	// tell the parent we've quit
	context.parent ! Quit
  }

  def receive = {
	// NewWebSocket is a request, and we respond with our Enumerator/Iteratee pair
	case NewWebSocket => {
	  sender ! WebSocketResponse(in, out)
	}
	// Standard application messages just get pushed into the Enumerator and up the websocket
	case msg:JsValue => {
	  out.push(msg)
	}
	case _ => {
	  Logger("WebSocketActor").info("Got a message we don't understand")
	}
  }
}

/**
 * This class oversees the communication between children WebSocketActor instances.
 * The default implementation just copies messages between children, but you can extend
 * and override handleJsMessage to add business logic
 */
class WebSocketMaster extends Actor {

  // list of child ActorRefs.  One per websocket client.
  var children = List.empty[ActorRef]

  // handle a javascript message from the websocket.
  def handleJsMessage(msg: JsValue) = {
	  // just forward it on to all the children
	  children.foreach { child =>
		child ! msg
	  }
  }

  def receive = {
	case msg: JsValue =>
	  handleJsMessage(msg)

	case NewWebSocket =>
	  // This is called when a new client connects to the websocket.  We create
	  // a new child Actor and forward this message on to it.
	  Logger("WebSocketMaster").info("got a request for a new websocket")
	  val newActorRef = context.actorOf(Props[WebSocketActor])
	  children = newActorRef :: children
	  newActorRef forward NewWebSocket

	case Quit =>
	  // A client has disconnected from the websocket
	  Logger("WebSocketMaster").info("Got a quit message, removing child")
	  children = children.filterNot(_ == sender)

	case _ =>
	  Logger("WebSocketMaster").info("Got a message we don't understand")
  }
}

