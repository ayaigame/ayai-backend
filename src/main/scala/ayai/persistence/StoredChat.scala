package ayai.persistence
/**
 * ayai.persistence.StoredChat
 * Database object for storing Chats
 */

/** External Imports */
import scala.slick.driver.H2Driver.simple._

case class StoredChat(id: Int, message: String, senderID: Int, receiverID: Int, received: Boolean)
case class NewStoredChat(message: String, senderID: Int, receiverID: Int, received: Boolean)

object StoredChats extends Table[StoredChat]("messages") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")
  def senderID = column[Int]("sender")
//  def sender = foreignKey("fk_sender", senderID, Users)(_.id.?)
  def receiverID = column[Int]("receiver")
 // def receiver = foreignKey("fk_receiver", receiverID, Users)(_.id.?)

  def received = column[Boolean]("received")

  def * = id ~ message ~ senderID ~ receiverID ~ received <> (StoredChat, StoredChat.unapply _)
  def autoInc = message ~ senderID ~ receiverID ~ received <> (NewStoredChat, NewStoredChat.unapply _) returning id
}
