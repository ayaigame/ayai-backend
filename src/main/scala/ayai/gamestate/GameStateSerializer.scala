package ayai.gamestate

/** Crane Imports **/
import crane.{Entity, World}
/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef}

/** Ayai Imports **/
import ayai.components.{Character, Position, Health, Room, TileMap, Inventory, Mana, Attack}

/** External Imports **/
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

sealed trait QueryType
sealed trait QueryResponse

case class CharacterRadius(characterId: String) extends QueryType
case class CharacterResponse(json: String)  extends QueryResponse
case class MapRequest(room : Entity)
case class SomeData

class GameStateSerializer(world: World, loadRadius: Int) extends Actor {


  //Returns a list of entities contained within a room.
  def getRoomEntities(roomId: Int): ArrayBuffer[Entity] = {
    world.groups("ROOM" + roomId)
  }

  //Returns a character's belongings and surroundings.
  def getCharacterRadius(characterId: String) = {
    val characterEntity : Entity = (world.getEntityByTag("CHARACTER" + characterId)) match {
      case Some(entity : Entity) => entity
    }
    
    val room = (characterEntity.getComponent(classOf[Room])) match {
      case Some(r : Room) => r 
    }
    

    val otherEntities: ArrayBuffer[Entity] = getRoomEntities(room.id)

//    var json = "{\"type\" : \"update\", \"you\": " + getEntityInfo(characterEntity) + ", \"others\": ["
    var entityJSONs = ArrayBuffer.empty[Entity]
    for(otherEntity <- otherEntities) {
      if(characterEntity != otherEntity) {
        //HACK FIX THIS WITH NEW ENTITY SYSTEM
        if (otherEntity.getComponent(classOf[Attack]).isEmpty) {
          entityJSONs += otherEntity
        }
      }
    }
    val jsonLift = 
      ("type" -> "update") ~
      ("you" -> ((characterEntity.getComponent(classOf[Character]),
        characterEntity.getComponent(classOf[Position]),
        characterEntity.getComponent(classOf[Health]),
        characterEntity.getComponent(classOf[Inventory]),
        characterEntity.getComponent(classOf[Mana])) match {
          case (Some(character : Character), Some(position : Position), Some(health : Health), Some(inventory : Inventory), Some(mana : Mana)) =>
            ((character.asJson()) ~
            (position.asJson) ~
            (health.asJson) ~
            (inventory.asJson) ~
            (mana.asJson))
        })) ~
       ("others" -> entityJSONs.map{ e => 
        (e.getComponent(classOf[Character]),
          e.getComponent(classOf[Position]),
          e.getComponent(classOf[Health]),
          e.getComponent(classOf[Mana])) match {
          case (Some(character : Character), Some(position : Position), Some(health : Health), Some(mana : Mana)) =>
            ((character.asJson()) ~
            (position.asJson) ~
            (health.asJson) ~
            (mana.asJson))
        }})

    sender ! compact(render(jsonLift))
    // sender ! new CharacterResponse(json)
  }

  //Once characters actually have belonging we'll want to implement this and use it in getCharacterRadius
  // def getCharacterBelongings(characterId: String) = {

  // }

  // def getSurroundings(pos: Position) = {

  // }

  def sendMapInfo(room : Entity ) = {
    val tilemap = (room.getComponent(classOf[TileMap])) match {
      case (Some(tileMap : TileMap)) => tileMap
    } 
    var json = ("type" -> "map") ~
      ("tilemap" -> tilemap.file) ~
      (tilemap.tilesets.asJson)

    println(compact(render(json)))
    sender ! compact(render(json))
  }

  def receive = {
    case CharacterRadius(characterId) => getCharacterRadius(characterId)
    case MapRequest(room) => sendMapInfo(room)
    case _ => println("Error: from serializer.")
  }

   // { "map" : {
   //    "root" : "assets/maps"
   //    "tilemap" : "map3.json",
   //    "tilesets" : [
   //       {"image" : "sd33.png" },
   //       {"image" : "blah_blah.png" }
   //    ]
   //  }}
}
