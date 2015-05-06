package ayai.persistence

/** External Imports **/
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import org.mindrot.jbcrypt.BCrypt

object AccountTable {
  def registerUser(username: String, password: String) = {
    getAccount(username) match {
      //If an account is found then the username is taken.
      case Some(account: Account) => false
      case _ => {
        Class.forName("org.h2.Driver")
        SessionFactory.concreteFactory = Some(() =>
          Session.create(
            java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
            new H2Adapter))

        transaction {
          AyaiDB.accounts.insert(new Account(username, BCrypt.hashpw(password, BCrypt.gensalt())))
        }

        getAccount(username) match {
          case Some(account: Account) => true
          case _ => throw new Exception("Account creation failed!")
        }
      }
    }
  }

  def getAccount(username: String): Option[Account] = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      val accountQuery = AyaiDB.accounts.where(account => account.username === username)
      if (accountQuery.size == 1)
        Some(accountQuery.single)
      else
        None
    }
  }

  def getAccountById(id: Long): Option[Account] = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      val accountQuery = AyaiDB.accounts.where(account => account.id === id)
      if (accountQuery.size == 1)
        Some(accountQuery.single)
      else
        None
    }
  }

  def createToken(account: Account): String = {
    val token = java.util.UUID.randomUUID.toString

    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      AyaiDB.tokens.insert(new Token(account.id, token))
    }

    token
  }

  def validatePassword(username: String, password: String): String  = {
    getAccount(username) match {
      case Some(account: Account) =>
        if (BCrypt.checkpw(password, account.password)) {
          createToken(account)
        } else {
          ""
        }
      case _ => ""
    }
  }

  //Returns the account id if the token is valid or -1 if its not.
  def getAccountIdFromToken(token: String): Long = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      val tokenQuery = AyaiDB.tokens.where(tokenRow => tokenRow.token === token)

      if (tokenQuery.size == 1)
        return tokenQuery.single.account_id
      else
        return -1
    }
  }
}