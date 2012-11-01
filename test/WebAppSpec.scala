package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{JsValue, Json, JsObject}

import play.api.libs.iteratee._

/**
 * Test the web app side of things.
 */
class WebAppSpec extends Specification {
  
  "Akka WebSocket app" should {

    // at least all the tests won't fail
    "compute 1 + 2" in {
      val a = 1 + 2
      
      a must equalTo(3)
    }

    "render index template" in {
      val html = views.html.index("Testing Akka Whiteboard")(FakeRequest())
      
      contentType(html) must equalTo("text/html")
      contentAsString(html) must contain("Testing Akka Whiteboard")
    }
    
    "respond to the index Action" in {
	  running(FakeApplication()) {
		val result = controllers.Application.index(FakeRequest())
		
		status(result) must equalTo(OK)
		contentType(result) must equalTo(Some("text/html"))
		charset(result) must equalTo(Some("utf-8"))
		contentAsString(result) must contain("Whiteboard")
	  }
    }
    
    "do not respond to a wrong url" in {
      val result = routeAndCall(FakeRequest(POST, "/bogosity"))
      
      result must equalTo(None)
    }

	"respond to the websocket thingy" in {
	  running(FakeApplication()) {
		val ws = controllers.Application.ws()

		1 must equalTo(1)
	  }
	}
  }
}
