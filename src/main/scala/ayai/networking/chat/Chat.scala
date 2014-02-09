package ayai.networking.chat
/**
 * ayai.networking.chat.Chat
 * Classes and Traits used in the Messaging services
 */

/** Ayai Imports **/
import ayai.persistence.Account

trait Chat { 
  val text: String
  val sender: Account
}

case class PublicChat(text: String, sender: Account) extends Chat
case class PrivateChat(text: String, sender: Account, receiver: Account) extends Chat
case class ChatHolder(held: Chat)

case class CheckIn(receiver: Account)
