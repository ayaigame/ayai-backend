package ayai.persistence

import ayai.components.Character
import ayai.apps.Constants

import scala.collection.mutable.ArrayBuffer

import crane.Entity

/** External Imports **/
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import org.mindrot.jbcrypt.BCrypt 

/** Socko Imports **/
import org.mashupbots.socko.events.{HttpRequestEvent, HttpResponseStatus}

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

object CharacterTable {

  def createCharacter(characterName: String, className: String, accountId: Long, startingRoom: Long, startingX: Int, startingY: Int) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
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
        set(dbCharacter.experience := character.experience,
            dbCharacter.name := character.name)) //Just did this to keep this syntax here, REMOVE THIS
    }
  }

  def characterList(request: HttpRequestEvent, accountId: Long) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      val characters =
        from(AyaiDB.characters)(c=>
          where(c.account_id === accountId)
          select(c)
        )

      var characterArray = new ArrayBuffer[JObject]()
      for(character <- characters) {
        characterArray += 
          ("name" -> character.name) ~
          ("level" -> Constants.EXPERIENCE_ARRAY.indexWhere((exp: Int) => character.experience < exp)) ~
          ("class" -> character.className)
      }

      request.response.write(HttpResponseStatus.OK, compact(render(characterArray)))
    }
  }
}
