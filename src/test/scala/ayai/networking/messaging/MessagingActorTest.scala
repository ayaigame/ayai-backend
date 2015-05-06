/*
package ayai.networking.messaging


/** Ayai Imports **/
import ayai.persistence.User

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, DefaultTimeout}

/** External Imports **/
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.MustMatchers


class MessagingTest extends TestKit(ActorSystem("testSystem")) with WordSpecLike with MustMatchers with BeforeAndAfter{

  val u = new User(1, "tim", "tim")
  val prm = new PrivateMessage("A special hello to you", u, u)
  val prmh = new MessageHolder(prm)

  val pum = new PublicMessage("Hello to everyone!", u)
  val pumh = new MessageHolder(pum)

  var ms: ActorRef = _
  var mr: ActorRef = _

  "A message receiver" must {
    // Creation of the TestActorRef
    val actorRef = TestActorRef[MessageReceiver]
    ms = system.actorOf(Props[MessageSender], name="ms1")

    "receive the right type of messages" in {
      actorRef ! pumh
      actorRef.underlyingActor.typeOfMessage must equal("public")
      actorRef ! prmh
      actorRef.underlyingActor.typeOfMessage must equal("private")
    }
  }

  "A message sender" must {
    "receive a private message" in {
      val testUser = new User(2, "rob", "rob")
      val testPM = new PrivateMessage("Another special hello", testUser, testUser)
      val testMH = new MessageHolder(testPM)

      val actorRef = TestActorRef[MessageSender]("ms2")
      mr = system.actorOf(Props[MessageSender], name="mr2")
      actorRef ! testMH
      actorRef.underlyingActor.lastMessage must equal(testPM.message)
    }
    "receive a public message" in {
      val actorRef = TestActorRef[MessageSender]("msp")
      mr = system.actorOf(Props[MessageSender], name="mr3")
      actorRef ! pumh
      actorRef.underlyingActor.lastMessage must equal(pum.message)
    }
  }
  // TODO: Test the entire loop using both receiver/sender
}
*/
