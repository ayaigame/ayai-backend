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

//Temporary for as long as initial inserts happen here.
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter

object DBCreation {
  def ensureDbExists() = {

    // If DB Doesn't exist, create it
    if(Files.exists(Paths.get("ayai.h2.db"))){
      Files.delete(Paths.get("ayai.h2.db"))
    }


    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
          new H2Adapter))
      transaction {
        AyaiDB.create
        // AyaiDB.printDdl
      }

    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
          new H2Adapter))

    var account: Long = 0

    transaction {
      AyaiDB.accounts.deleteWhere(a =>
        (1 === 1))
      AyaiDB.characters.deleteWhere(a =>
        (1 === 1))
    }

    val tim = new Account("tim", BCrypt.hashpw("tim", BCrypt.gensalt()))

    transaction {
      AyaiDB.accounts.insert(tim)
    }

    account = AyaiDB.getAccount("tim").id

    transaction {
      AyaiDB.characters.insert(new CharacterRow("Orunin", "Paladin", 0, account, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
      AyaiDB.characters.insert(new CharacterRow("Xanthar", "Mage", 0, account, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
      AyaiDB.characters.insert(new CharacterRow("Ness", "Warrior", 0, account, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
      AyaiDB.characters.insert(new CharacterRow("Ezio", "Thief", 0, account, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
    }
  }
}
