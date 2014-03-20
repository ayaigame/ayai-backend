package ayai.persistence

import ayai.components.{Character, Position, Room}
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

  //This should check for an existing character with characterName
  def createCharacter(request: HttpRequestEvent, characterName: String, className: String, accountId: Long) {
    this.createCharacter(request, characterName, className, accountId, Constants.STARTING_ROOM_ID, Constants.STARTING_X, Constants.STARTING_Y)
  }

  def createCharacter(request: HttpRequestEvent,
                      characterName: String,
                      className: String,
                      accountId: Long,
                      startingRoom: Long,
                      startingX: Int,
                      startingY: Int) {
    getCharacter(characterName) match {
      case Some(character: CharacterRow) =>
        request.response.write(HttpResponseStatus.CONFLICT)

      case _ =>
        Class.forName("org.h2.Driver");
        SessionFactory.concreteFactory = Some (() =>
            Session.create(
            java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
            new H2Adapter))

        transaction {
          AyaiDB.characters.insert(new CharacterRow(characterName, className, 0, accountId, startingRoom, startingX, startingY))
          val characterQuery =
            from(AyaiDB.characters)(row =>
              where(row.name === characterName)
              select(row)
            )

          //give them some starting items
          AyaiDB.inventory.insert(new InventoryRow(characterQuery.single.id, 0, 1))
          AyaiDB.inventory.insert(new InventoryRow(characterQuery.single.id, 1, 1))
          AyaiDB.inventory.insert(new InventoryRow(characterQuery.single.id, 2, 1))

          request.response.write(HttpResponseStatus.OK, "GOOD")
        }
    }
  }

  def getCharacter(characterName: String): Option[CharacterRow] = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      val characterQuery = AyaiDB.characters.where(character => character.name === characterName)
      if(characterQuery.size == 1)
        Some(characterQuery.single)
      else
        None
    }
  }

  def getCharacter(characterId: Long) = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
        Session.create(
        java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
        new H2Adapter))

    transaction {
      val characterQuery = AyaiDB.characters.where(character => character.id === characterId)
      if(characterQuery.size == 1)
        Some(characterQuery.single)
      else
        None
    }
  }

  def saveCharacter(entity: Entity) = {
  (entity.getComponent(classOf[Position]),
    entity.getComponent(classOf[Character]),
    entity.getComponent(classOf[Room])) match {
      case(Some(position : Position), Some(character : Character), Some(room : Room)) =>
        Class.forName("org.h2.Driver");
        SessionFactory.concreteFactory = Some (() =>
            Session.create(
            java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
            new H2Adapter))

        transaction {
          update(AyaiDB.characters)(dbCharacter =>
            where(dbCharacter.name === character.name)
            set(dbCharacter.experience := character.experience,
                dbCharacter.pos_x := position.x,
                dbCharacter.pos_y := position.y,
                dbCharacter.room_id := room.id))
        }
      case _ =>
        println("Can't get entites for some reason.")
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
        from(AyaiDB.characters)(character =>
          where(character.account_id === accountId)
          select(character)
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