package controllers

import com.bmdhacks.akkawebsocket._

import play.api._
import play.api.mvc._

import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.libs.json._

import play.api.Play.current

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  val wsMaster = Akka.system.actorOf(Props[TronGame])

  def ws = WebSocket.async[JsValue] { request =>
	Logger("application").info("Inside of websocket handler")
    implicit val timeout = Timeout(1 second)
	(wsMaster ? NewWebSocket).asPromise map {
	  case WebSocketResponse(in, out) =>
		Logger("application").info("Got a websocket response to initialize websocket: " + in.toString + " " + out.toString)
		(in, out)
	}
  }
}
