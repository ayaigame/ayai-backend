import akka.actor.Actor
import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, DefaultTimeout}
import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpecLike
import akka.testkit.ImplicitSender

 
// Just a simple actor
class SimpleActor extends Actor {
 
  // Sample actor internal state
  var lastMsg: String = ""
 
  def receive = {
    case msg: String => {
      // Storing the message in the internal state variable
      lastMsg = msg
    }
  }
}
 
class SimpleTest extends TestKit(ActorSystem("testSystem")) with WordSpecLike with MustMatchers {
 
  "A simple actor" must {
    // Creation of the TestActorRef
    val actorRef = TestActorRef[SimpleActor]
 
    "receive messages" in {
      // This call is synchronous. The actor receive() method will be called in the current thread
      actorRef ! "world"
      // With actorRef.underlyingActor, we can access the SimpleActor instance created by Akka
      actorRef.underlyingActor.lastMsg must equal("world")
    }
  }
}
