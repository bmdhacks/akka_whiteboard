
package test

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions

import akka.actor._
import akka.testkit._
import akka.util.duration._

import play.api.libs.json._
import play.api.libs.iteratee._

import com.bmdhacks.akkawebsocket._

/* A tiny class that can be used as a Specs2 'context'. */
abstract class AkkaTestkitSpecs2Support extends TestKit(ActorSystem()) 
                                           with After 
                                           with ImplicitSender {
  // make sure we shut down the actor system after all tests have run
  def after = system.shutdown()
}
 
/* Both Akka and Specs2 add implicit conversions for adding time-related
   methods to Int. Mix in the Specs2 NoTimeConversions trait to avoid a clash. */
class AkkaWebSocketSpec extends Specification with NoTimeConversions {

  /* by default, test cases are run concurrently, which might get confusing if we test
   * the same actor in two test cases.  This makes them sequential */
  sequential
 
  "AkkaWebSocket" should {
    /* for every case where you would normally use "in", use 
       "in new AkkaTestkitSpecs2Support" to create a new 'context'. */
    "work properly with Specs2 unit tests" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        system.actorOf(Props(new Actor {
          def receive = { case x ⇒ sender ! x }
        })) ! "hallo"
		
        expectMsgType[String] must be equalTo "hallo"
      }
    }
	
	"create new web socket objects" in new AkkaTestkitSpecs2Support {
	  within (1 second) {
		system.actorOf(Props[WebSocketActor]) ! NewWebSocket
		expectMsgType[WebSocketResponse]
	  }
	}

	"create the master actor" in new AkkaTestkitSpecs2Support {
	  within (1 second) {
		system.actorOf(Props[WebSocketMaster]) ! NewWebSocket
		expectMsgType[WebSocketResponse]
	  }
	}

	"create sub-actors through the master actor" in new AkkaTestkitSpecs2Support {
	  within (1 second) {
		val master = system.actorOf(Props[WebSocketMaster])
		master ! NewWebSocket
		expectMsgType[WebSocketResponse]
	  }
	}
  }
}
