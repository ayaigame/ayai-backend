package ayai.persistence

import ayai.networking.CharacterList

import scala.collection.mutable.ArrayBuffer

/** Akka Imports **/
import akka.actor.Actor

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

class CharacterTable extends Actor {
  def getCharacterListJson(webSocket: WebSocketFrameEvent, accountName: String) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      val account = AyaiDB.getAccount("tim").id

      val characters =
        from(AyaiDB.characters)(c=>
          where(c.account_id === account)
          select(c)
        )

      var characterArray = new ArrayBuffer[JObject]()
      for(character <- characters) {
        characterArray += 
          ("name" -> character.name) ~
          ("class" -> character.className) ~
          ("level" -> character.level)
      }

      val jsonLift = 
        ("type" -> "charList") ~
        ("chars" -> characterArray)

      webSocket.writeText(compact(render(jsonLift)))
    }
  }

  def saveCharacter(character: String) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    // transaction {
    //   update(AyaiDB.characters)(dbCharacter => 
    //     where(dbCharacter.name === character.name)
    //     set(dbCharacter.experience := character.experience,
    //         dbCharacter.level := character.level))
    // }
  }

   def receive = {
    case CharacterList(webSocket, accountName) => getCharacterListJson(webSocket, accountName)
    case _ => println("Error: from CharacterTable.")
  }
}
