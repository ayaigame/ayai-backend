package ayai.persistence
/**
 * ayai.persistence.StoredChat
 * Database object for storing Chats
 */

//import org.squeryl.PrimitiveTypeMode._
//import org.squeryl.dsl._
//import org.squeryl.Schema
//import org.squeryl.annotations.Column
//import java.util.Date
//import java.sql.Timestamp
//
//class Chat(val id: Long,
//           val message: String,
//           val senderId: Long,
//           val receiverId: Long,
//           var received: Boolean) {
//            def this() = this(0, "", "")
//            }
//
//object Accounts extends Schema {
//  val accounts = table[Account]
//}
//
////case class StoredChat(id: Int, message: String, senderID: Int, receiverID: Int, received: Boolean)
////case class NewStoredChat(message: String, senderID: Int, receiverID: Int, received: Boolean)
//
////object StoredChats extends Table[StoredChat]("messages") {
////  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
////  def message = column[String]("message")
////  def senderID = column[Int]("sender")
//////  def sender = foreignKey("fk_sender", senderID, Users)(_.id.?)
////  def receiverID = column[Int]("receiver")
//// // def receiver = foreignKey("fk_receiver", receiverID, Users)(_.id.?)
////
////  def received = column[Boolean]("received")
////
////  def * = id ~ message ~ senderID ~ receiverID ~ received <> (StoredChat, StoredChat.unapply _)
////  def autoInc = message ~ senderID ~ receiverID ~ received <> (NewStoredChat, NewStoredChat.unapply _) returning id
////}
