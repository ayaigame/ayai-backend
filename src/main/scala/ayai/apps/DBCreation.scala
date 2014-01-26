package ayai.apps

/**
 * ayai.apps.DBCreation
 * Creates DB
 */

/** Ayai Imports **/
import ayai.persistence._

/** External Imports **/
import java.nio.file.{Files, Paths}
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import org.mindrot.jbcrypt.BCrypt 


object DBCreation {
  def main(args: Array[String]) = {
    // If DB Doesn't exist, create it
    if(!Files.exists(Paths.get("ayai.h2.db"))) {
      Class.forName("org.h2.Driver");
      SessionFactory.concreteFactory = Some (() =>
          Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
          new H2Adapter))
        transaction {
          Accounts.create
          Accounts.printDdl
           
        }
      } else {
       Class.forName("org.h2.Driver");
      SessionFactory.concreteFactory = Some (() =>
          Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
          new H2Adapter))
      transaction {
        val tim = Accounts.accounts.insert(new Account("tim", BCrypt.hashpw("tim", BCrypt.gensalt())))
        val account = Accounts.getAccount("tim")
        println(account)
        val token = Accounts.validatePassword("tim", "tim")
        println(token)
        
      }
    }
  }
}
