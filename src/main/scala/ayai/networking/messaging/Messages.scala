package ayai.networking.messaging
/**
 * ayai.networking.messaging.Message
 * Classes and Traits used in the Messaging services
 */

/** Ayai Imports **/
import ayai.persistence.User

trait Message { 
  val message: String
  val sender: User
}

case class PublicMessage(message: String, sender: User) extends Message
case class PrivateMessage(message: String, sender: User, receiver: User) extends Message
case class MessageHolder(held: Message)

case class CheckIn(receiver: User)
