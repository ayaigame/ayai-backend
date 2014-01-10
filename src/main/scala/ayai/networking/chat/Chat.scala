package ayai.networking.chat
/**
 * ayai.networking.chat.Chat
 * Classes and Traits used in the Messaging services
 */

/** Ayai Imports **/
import ayai.persistence.User

trait Chat { 
  val text: String
  val sender: User
}

case class PublicChat(text: String, sender: User) extends Chat
case class PrivateChat(text: String, sender: User, receiver: User) extends Chat
case class ChatHolder(held: Chat)

case class CheckIn(receiver: User)
