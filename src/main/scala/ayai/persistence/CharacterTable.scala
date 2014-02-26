package ayai.persistence

import ayai.networking.CharacterList
import ayai.components.Character

import scala.collection.mutable.ArrayBuffer

/** Akka Imports **/
import akka.actor.Actor

import crane.Entity

/** External Imports **/
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import org.mindrot.jbcrypt.BCrypt 

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

case class RetrieveCharacter(characterId: Long)
case class RetrievedCharacter(character: CharacterRow)

class CharacterTable extends Actor {

  def createCharacter(characterName: String, className: String, accountId: Long, startingRoom: Long, startingX: Int, startingY: Int) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some(() =>
        Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"), 
          new H2Adapter))
    transaction {
      AyaiDB.characters.insert(new CharacterRow(characterName, characterName, 0, accountId, startingRoom, startingX, startingY))
    }
  }

  def saveCharacter(character: Character) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
          new H2Adapter))

    transaction {
      update(AyaiDB.characters)(dbCharacter => 
        where(dbCharacter.name === character.name)
        set(dbCharacter.experience := character.experience.toLong,
            dbCharacter.name := character.name)) //Just did this to keep this syntax here, REMOVE THIS
    }
  }

   def receive = {
    case CharacterList(id, accountName) => {
      Class.forName("org.h2.Driver");
      SessionFactory.concreteFactory = Some (() =>
          Session.create(
          java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
          new H2Adapter))

      transaction {
        val account = AyaiDB.getAccount("tim").id

        val characters =
          from(AyaiDB.characters)( c =>
            where(c.account_id === account)
            select(c)
          )

        var characterArray = new ArrayBuffer[JObject]()
        for(character <- characters) {
          characterArray += 
            ("name" -> character.name) ~
            ("class" -> character.className)
        }

        val jsonLift = 
          ("type" -> "charList") ~
          ("chars" -> characterArray)

        // TODO: Replace with selection
        // webSocket.writeText(compact(render(jsonLift)))
      }
    }

    case RetrieveCharacter(characterId: Long) => {

    }
    
    case _ => println("Error: from CharacterTable.")
  }
}
