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
    println("Recreating DataBase....")

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

    // Class.forName("org.h2.Driver");
    // SessionFactory.concreteFactory = Some (() =>
    //     Session.create(
    //       java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
    //       new H2Adapter))

    // var account: Long = 0

    // transaction {
    //   AyaiDB.accounts.deleteWhere(a =>
    //     (1 === 1))
    //   AyaiDB.characters.deleteWhere(a =>
    //     (1 === 1))
    // }

    val defaultUser = new Account("tim", BCrypt.hashpw("defaultUser", BCrypt.gensalt()))

    transaction {
      AyaiDB.accounts.insert(defaultUser)
    }

    AccountTable.getAccount("tim") match {
      case Some(account: Account) =>
        transaction {
          AyaiDB.characters.insert(new CharacterRow("Orunin", "Paladin", 1, 0, account.id, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
          AyaiDB.characters.insert(new CharacterRow("Xanthar", "Mage", 1, 0, account.id, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
          AyaiDB.characters.insert(new CharacterRow("Ness", "Warrior", 1, 0, account.id, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
          AyaiDB.characters.insert(new CharacterRow("Ezio", "Thief", 1, 0, account.id, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y))
        }

        transaction {
          CharacterTable.getCharacter("Ness") match {
            case Some(characterRow: CharacterRow) =>
              AyaiDB.inventory.insert(new InventoryRow(characterRow.id, 0, 1))
              AyaiDB.inventory.insert(new InventoryRow(characterRow.id, 3, 1))
            case _ =>
          }
        }
      case _ =>
        throw(new Exception("Account creation failed!"))
    }
  }
}
