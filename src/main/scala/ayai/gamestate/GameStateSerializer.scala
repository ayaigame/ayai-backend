package ayai.gamestate

/** Artemis Imports **/
import com.artemis.{Entity, World}
import com.artemis.managers.{TagManager, GroupManager}
import com.artemis.utils.{Bag, ImmutableBag}
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
  def getRoomEntities(roomId: Int): ImmutableBag[Entity] = {
    world.getManager(classOf[GroupManager]).getEntities("ROOM" + roomId)
  }

  //Returns a character's belongings and surroundings.
  def getCharacterRadius(characterId: String) = {
    val characterEntity : Entity = world.getManager(classOf[TagManager]).getEntity("CHARACTER" + characterId)
    val room = characterEntity.getComponent(classOf[Room])

    val otherEntities: ImmutableBag[Entity] = getRoomEntities(room.id)

//    var json = "{\"type\" : \"update\", \"you\": " + getEntityInfo(characterEntity) + ", \"others\": ["
    var entityJSONs = new ArrayBuffer[Entity]()
    for(i <- 0 until otherEntities.size()) {
      if(characterEntity.getId() != otherEntities.get(i).getId()) {
        //HACK FIX THIS WITH NEW ENTITY SYSTEM
        if (otherEntities.get(i).getComponent(classOf[Attack]) == null) {
          entityJSONs += otherEntities.get(i)
        }
      }
    }
    val jsonLift = 
      ("type" -> "update") ~
      ("you" -> ((characterEntity.getComponent(classOf[Character]).asJson()) ~
        (characterEntity.getComponent(classOf[Position]).asJson) ~
        (characterEntity.getComponent(classOf[Health]).asJson) ~
        (characterEntity.getComponent(classOf[Inventory]).asJson) ~
        (characterEntity.getComponent(classOf[Mana]).asJson))) ~
       ("others" -> entityJSONs.map{ e => 
        ((e.getComponent(classOf[Character]).asJson()) ~
        (e.getComponent(classOf[Position]).asJson) ~
        (e.getComponent(classOf[Health]).asJson) ~
        (e.getComponent(classOf[Mana]).asJson))})

    sender ! compact(render(jsonLift))
    // sender ! new CharacterResponse(json)
  }

  //Once characters actually have belonging we'll want to implement this and use it in getCharacterRadius
  // def getCharacterBelongings(characterId: String) = {

  // }

  // def getSurroundings(pos: Position) = {

  // }

  def sendMapInfo(room : Entity ) = {
    val tilemap = room.getComponent(classOf[TileMap]) 
    var json = ("type" -> "map") ~
      ("tilemap" -> tilemap.file) ~
      (room.getComponent(classOf[TileMap]).tilesets.asJson)

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
