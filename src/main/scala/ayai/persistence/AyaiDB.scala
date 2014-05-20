package ayai.persistence
/**
 * ayai.persistence.Account
 * Database object for storing Account
 */

import ayai.apps.Constants //Only necessary to create a character for each account.
import ayai.networking.chat.{PublicChat, PrivateChat}

import org.squeryl.{Schema, KeyedEntity}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.mindrot.jbcrypt.BCrypt
import org.squeryl.dsl.CompositeKey2
import java.util.Date
import java.sql.Timestamp

//It's possible we only want to do reads and writes from a table class
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter

object AyaiDB extends Schema {
  val accounts = table[Account]("ACCOUNTS")
  val chats = table[Chat]
  val tokens = table[Token]
  val characters = table[CharacterRow]("CHARACTERS")
  val inventory = table[InventoryRow]("INVENTORY")
  val equipment = table[EquipmentRow]("EQUIPMENT")
  val senderToChat = oneToManyRelation(characters, chats).via((a, b) => a.id === b.sender_id)
  val receiverToChat = oneToManyRelation(characters, chats).via((a, b) => a.id === b.receiver_id)
  val accountToToken = oneToManyRelation(characters, tokens).via((a, b) => a.id === b.account_id)

  on(accounts)(account => declare(
    account.username is(unique)
  ))

  on(characters)(character => declare(
    character.name is(unique)
  ))

  def storePublicChat(chat: PublicChat) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      chats.insert(new Chat(chat.text, chat.sender.id, None, true))
    }
  }
}

case class Account(
              val username: String,
              var password: String)
            extends AccountDb2Object{
                def this() = this("", "")
            }
case class Token(
              val account_id: Long,
              val token: String) extends AccountDb2Object {
                def this() = this(0, "")
                lazy val user = AyaiDB.accountToToken.right(this)
              }

case class Chat(
             val message: String,
             val sender_id: Long,
             val receiver_id: Option[Long],
             var received: Boolean)
           extends AccountDb2Object {
             def this() = this("", 0, Some(0), false)
             lazy val sender = AyaiDB.senderToChat.right(this)
             lazy val receiver = AyaiDB.receiverToChat.right(this)
}

case class CharacterRow (
            val name: String,
            val className: String,
            val level: Int,
            val experience: Long,
            val account_id: Long,
            val room_id: Long,
            val pos_x: Int,
            val pos_y: Int)
          extends AccountDb2Object {
            def this() = this("", "", 1, 0, 0, 1, 0, 0)
            lazy val sentChats = AyaiDB.senderToChat.left(this)
            lazy val receivedChats = AyaiDB.receiverToChat.left(this)
            lazy val registeredTokens = AyaiDB.accountToToken.left(this)
}

case class InventoryRow (
            val characterId: Long,
            val itemId: Long,
            val quantity: Long)
          extends KeyedEntity[CompositeKey2[Long,Long]] {
            def id = compositeKey(characterId, itemId)
            def this() = this(0, 0, 0)
}

case class EquipmentRow (
            val characterId: Long,
            val itemId: Long,
            val slot: String)
          extends AccountDb2Object {
            def this() = this(0, 0, "")
}
class AccountDb2Object extends KeyedEntity[Long] {
  val id: Long = 0
}
