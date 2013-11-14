package ayai.persistence
/**
 * ayai.persistence.StoredMessage
 * Database object for storing Messages
 */

/** External Imports */
import scala.slick.driver.H2Driver.simple._

case class StoredMessage(id: Int, message: String, senderID: Int, receiverID: Int, received: Boolean)
case class NewStoredMessage(message: String, senderID: Int, receiverID: Int, received: Boolean)

object StoredMessages extends Table[StoredMessage]("messages") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")
  def senderID = column[Int]("sender")
//  def sender = foreignKey("fk_sender", senderID, Users)(_.id.?)
  def receiverID = column[Int]("receiver")
 // def receiver = foreignKey("fk_receiver", receiverID, Users)(_.id.?)

  def received = column[Boolean]("received")

  def * = id ~ message ~ senderID ~ receiverID ~ received <> (StoredMessage, StoredMessage.unapply _)
  def autoInc = message ~ senderID ~ receiverID ~ received <> (NewStoredMessage, NewStoredMessage.unapply _) returning id
}
