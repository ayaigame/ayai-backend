package ayai.networking.messaging
/**
 * ayai.networking.messaging.MessagingSendingService
 * Actor that receives messages sent by the player over sockets and stores them
 * and forwards them to the actor that forwards messages back to players
 */

/** Ayai Imports **/
import ayai.persistence.User
import ayai.networking.Connection

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

/** External Imports **/
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

trait Message
case class PublicMessage(message: String, sender: User) extends Message
case class PrivateMessage(message: String, sender: User, receiver: User) extends Message
case class MessageHolder(held: Message)

class MessageSendingService extends Actor {

  def receive = {
    case MessageHolder(message) =>
      store(message)

    case _ => println("Unknown message type")
   }

  def store(message: Message) ={
    message match {
      case PrivateMessage(message, sender, receiver) =>
        val received = false
        Database.forURL("jdbc:h2:mem:ayai", driver = "org.h2.Driver") withSession {
          StoredMessages.autoInc.insert(NewStoredMessage(message, sender.id, receiver.id, received))
        }
      case PublicMessage(message, sender) =>
        Database.forURL("jdbc:h2:mem:ayai", driver = "org.h2.Driver") withSession {
          StoredMessages.autoInc.insert(NewStoredMessage(message, sender.id, -1, true))
        }
    }
    true
  }
  
}
