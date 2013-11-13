package ayai.networking.messaging
/**
 * ayai.networking.messaging.MessageReceiver
 * Actor that receives messages sent by the player over sockets and stores them
 * and forwards them to the actor that forwards messages back to players
 */

/** Akka Imports **/
import akka.actor.Actor

/** External Imports **/
import scala.slick.driver.H2Driver.simple.{Database,Session}
import scala.concurrent.duration._
import scala.concurrent.Await


class MessageReceiver extends Actor {

  def receive = {
    case MessageHolder(message) =>
      val received = reroute(message)
      store(message, received)

    case _ => println("Unknown message type")
   }

  def store(message: Message, received: Boolean) = {
    message match {
      case PrivateMessage(message, sender, receiver) =>
        Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
          StoredMessages.autoInc.insert(NewStoredMessage(message, sender.id, receiver.id, received))
        }
      case PublicMessage(message, sender) =>
        Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
          StoredMessages.autoInc.insert(NewStoredMessage(message, sender.id, -1, true))
        }
    }
  }
  def reroute(message: Message) = {
    val targetSelection = context.system.actorSelection("user/" + 1).resolveOne(1.seconds)
    val targetRef = Await.result(targetSelection, 1.seconds)
    if(targetRef.isTerminated) {
      println("User not found/online")
      false
    } else {
      val messageHolder = new MessageHolder(message)
      targetRef ! messageHolder
      true

    }
  }
  
}
