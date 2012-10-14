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

case object Quit
case object NewWebSocket
case class WebSocketResponse(in: Iteratee[JsValue,_], out: Enumerator[JsValue])

// object WebSocketResponse {
//   // create an empty websocket response that just returns EOF
//   def empty = {
// 	// A finished Iteratee sending EOF
// 	val iteratee = Done[JsValue,Unit]((),Input.EOF)

//     // Send an error and close the socket
//     val enumerator =  Enumerator[JsValue](JsString("error"))

// 	WebSocketResponse(iteratee, enumerator)
//   }
// }

class WebSocketActor extends Actor {

  // this holds the stuff we want to push back out the websocket to the browser
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
	case NewWebSocket => {
	  Logger("WebSocketActor").info("Got a request for a new websocket")
	  sender ! WebSocketResponse(in, out)
	}
	case msg:JsValue => {
	  out.push(msg)
	  Logger("WebSocketActor").info("Got a message: " + msg)
	}
	case _ => {
	  Logger("WebSocketActor").info("Got a message we don't understand")
	}
  }
}

class WebSocketMaster extends Actor {
  implicit val timeout = Timeout(1 second)
  var children = List.empty[ActorRef]

  /* handle a javascript message from the websocket */
  def handleJsMessage(msg: JsValue) = {
	  children.foreach { child =>
		child ! msg
	  }
  }

  def receive = {
	case msg: JsValue =>
	  Logger("WebSocketMaster").info("got a message: " + msg)
	  handleJsMessage(msg)
	case NewWebSocket =>
	  Logger("WebSocketMaster").info("got a request for a new websocket")
	  val newActorRef = context.actorOf(Props[WebSocketActor])
	  children = newActorRef :: children
	  newActorRef forward NewWebSocket
	case Quit =>
	  Logger("WebSocketMaster").info("Got a quit message, removing child")
	  children = children.filterNot(_ == sender)
	case _ =>
	  Logger("WebSocketMaster").info("Got a message we don't understand")
  }
}

