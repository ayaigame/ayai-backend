package ayai.networking.messaging
/**
 * ayai.networking.messaging.MessageReceiver
 * Actor that receives messages sent by the player over sockets and stores them
 * and forwards them to the actor that forwards messages back to players
 */

/** Ayai Imports **/
import ayai.persistence.{StoredMessage, StoredMessages, NewStoredMessage}

/** Akka Imports **/
import akka.actor.Actor

/** External Imports **/
import scala.slick.driver.H2Driver.simple.{Database,Session}
import scala.concurrent.duration._
import scala.concurrent.Await


class MessageReceiver extends Actor {
  var typeOfMessage: String = ""

  def receive = {
    case MessageHolder(message) =>
      println("MessageHolder")
      val received = reroute(message)
      store(message, received)
      context.stop(self)

    case _ => println("Unknown message type")
   }

  private def store(message: Message, received: Boolean) = {
    var storedMessage = None : Option[NewStoredMessage]
    // Create the type of stored message based on the type of message we receive
    // This is since Public Messages do not have a reciever and are automatically
    // considered "received"
    message match {
      case PrivateMessage(message, sender, receiver) =>
        storedMessage = Some(NewStoredMessage(message, sender.id, receiver.id, received))
      case PublicMessage(message, sender) =>
        storedMessage = Some(NewStoredMessage(message, sender.id, -1, true))
    }

    // Insert the message into the DB
    storedMessage match {
      case None => 
        println("Should not get here - this is a private method")
      case Some(message) =>
        Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
          StoredMessages.autoInc.insert(message)
        }
    }  

  }

  def reroute(message: Message) : Boolean = {
    val messageHolder = new MessageHolder(message)

    message match {
      // Attempt to send Private message
      case PrivateMessage(message, sender, receiver) =>
        typeOfMessage = "private"
        val targetFuture = context.system.actorSelection("user/ms" + receiver.id).resolveOne(100.milliseconds)
        val targetRef = Await.result(targetFuture, 100.milliseconds)
        if(targetRef.isTerminated) {
          return false
        } else {
          targetRef ! messageHolder
          return true
        }
      // Send Public Message to every message sender
      case PublicMessage(message, sender) =>
        typeOfMessage = "public"
        val targetSelection = context.system.actorSelection("user/ms*")
        targetSelection ! messageHolder
        return false
    }
  }
}
