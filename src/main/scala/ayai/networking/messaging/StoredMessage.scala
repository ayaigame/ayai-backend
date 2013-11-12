package ayai.main.networking.messaging
import scala.slick.driver.H2Driver.simple._

case class StoredMessage(id: Option[Int], message: String, sender: String, receiver: String, received: Boolean)

object StoredMessages extends Table[StoredMessage]("messages") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")
  def sender = column[String]("sender")
  def receiver = column[String]("receiver")
  def received = column[Boolean]("received")
  def * = id.? ~ message ~ sender ~ receiver ~ received <> (StoredMessage, StoredMessage.unapply _)
}
