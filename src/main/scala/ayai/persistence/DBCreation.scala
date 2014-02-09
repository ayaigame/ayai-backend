package ayai.persistence

/**
 * ayai.apps.DBCreation
 * Creates DB
 */

import ayai.apps.Constants 

/** External Imports **/
import java.nio.file.{Files, Paths}
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import org.mindrot.jbcrypt.BCrypt 


object DBCreation {
  def ensureDbExists() = {
    // If DB doesn't exist, create it
    if(!Files.exists(Paths.get("ayai.h2.db"))) {
      Class.forName("org.h2.Driver");
      SessionFactory.concreteFactory = Some (() =>
          Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
          new H2Adapter))
        transaction {
          AyaiDB.create
          AyaiDB.printDdl
        }
      }

    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      AyaiDB.accounts.deleteWhere(a =>
        (1 === 1))

      val tim = new Account("tim", BCrypt.hashpw("tim", BCrypt.gensalt()))
      AyaiDB.accounts.insert(tim)
      val account = AyaiDB.getAccount("tim").id
      println(account)

      AyaiDB.characters.insert(new CharacterRow("Orunin", "Paladin", 1, 0, account, Constants.STARTING_ROOM_ID, 30, 30))
      AyaiDB.characters.insert(new CharacterRow("Xanthar", "Mage", 1, 0, account, Constants.STARTING_ROOM_ID, 30, 30))
    }
  }
}
