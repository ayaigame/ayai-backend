package ayai.persistence
/**
 * ayai.persistence.Account
 * Database object for storing Account
 */

import org.squeryl.{Schema, KeyedEntity}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.mindrot.jbcrypt.BCrypt 
import java.util.Date
import java.sql.Timestamp

object AyaiDB extends Schema {
  val accounts = table[Account]("ACCOUNTS")
  val chats = table[Chat]
  val tokens = table[Token]
  val senderToChat = oneToManyRelation(accounts, chats).via((a, b) => a.id === b.sender_id)
  val receiverToChat = oneToManyRelation(accounts, chats).via((a, b) => a.id === b.receiver_id)
  val accountToToken = oneToManyRelation(accounts, tokens).via((a, b) => a.id === b.user_id)

  on(accounts)(a => declare(
    a.username is(unique)
  ))

  def registerUser(username: String, password: String) = {accounts.insert(new Account(username, BCrypt.hashpw(password, BCrypt.gensalt())))}

  def getAccount(username: String) = {accounts.where(account => account.username === username).single}

  def createToken(account: Account): String = {
    val token = java.util.UUID.randomUUID.toString
    tokens.insert(new Token(account.id, token))
    return token
  }

  def validatePassword(username: String, password: String): String  = {
    val account = getAccount(username) 
    if(BCrypt.checkpw(password, account.password)) {
      return createToken(account)
    } else {
      return ""
   }
  }
}

case class Account(
              val username: String,
              var password: String) 
            extends AccountDb2Object{
                def this() = this("", "")
                lazy val sentChats = AyaiDB.senderToChat.left(this)
                lazy val receivedChats = AyaiDB.receiverToChat.left(this)
                lazy val registeredTokens = AyaiDB.accountToToken.left(this)
            }
case class Token(
              val user_id: Long,
              val token: String) extends AccountDb2Object { 
                def this() = this(0, "")
                lazy val user = AyaiDB.accountToToken.right(this)
              }

case class Chat(
           val message: String,
           val sender_id: Long,
           val receiver_id: Long,
           var received: Boolean) 
          extends AccountDb2Object {
            def this() = this("", 0, 0, false)
            lazy val sender = AyaiDB.senderToChat.right(this)
            lazy val receiver = AyaiDB.receiverToChat.right(this)
}



class AccountDb2Object extends KeyedEntity[Long] {
  val id: Long = 0
}
