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

  // only a single master Actor is created for the application
  val wsMaster = Akka.system.actorOf(Props[WebSocketMaster])

  def index = Action { implicit request =>
    Ok(views.html.index("Akka Whiteboard"))
  }

  // Play websocket interface.  We ask the master to create a new Actor, and
  // we are delivered the new Actor's Iteratee/Enumerator pair.
  def ws = WebSocket.async[JsValue] { request =>
    implicit val timeout = Timeout(1 second)
	(wsMaster ? NewWebSocket).asPromise map {
	  case WebSocketResponse(in, out) =>
		(in, out)
	}
  }
}
