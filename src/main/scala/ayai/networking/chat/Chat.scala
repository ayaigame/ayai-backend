package ayai.networking.chat
/**
 * ayai.networking.chat.Chat
 * Classes and Traits used in the Messaging services
 */

/** Ayai Imports **/
import ayai.persistence.CharacterRow
import crane.World

trait Chat { 
  val text: String
  val sender: CharacterRow
}

case class PublicChat(text: String, sender: CharacterRow) extends Chat
case class PrivateChat(text: String, sender: CharacterRow, receiver: CharacterRow) extends Chat
case class ChatHolder(held: Chat, world: World)

case class CheckIn(receiver: CharacterRow)
