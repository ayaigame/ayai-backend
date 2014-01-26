package ayai.gamestate

/** Artemis Imports **/
import com.artemis.{Entity, World}
import com.artemis.managers.{TagManager, GroupManager}
import com.artemis.utils.{Bag, ImmutableBag}

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef}

/** Ayai Imports **/
import ayai.components.{Character, Position, Health, Room, TileMap}

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

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
    val characterEntity : Entity = world.getManager(classOf[TagManager]).getEntity(characterId)
    val room = characterEntity.getComponent(classOf[Room])

    val otherEntities: ImmutableBag[Entity] = getRoomEntities(room.id)

    var json = "{\"type\" : \"update\", \"you\": " + getEntityInfo(characterEntity) + ", \"others\": ["
    var entityJSONs = new ArrayBuffer[String]()
    for(i <- 0 until otherEntities.size()) {
      if(characterEntity.getId() != otherEntities.get(i).getId()) {
        entityJSONs += getEntityInfo(otherEntities.get(i))
      }
    }

    //Kinda need a do-while because the first one doesn't prepend a comma
    if(entityJSONs.size > 0 ) {
      json = json + entityJSONs(0)
    }

    for(i <- 1 until entityJSONs.size) {
      json = json + ", " + entityJSONs(i)
    }

    json = json + "]}"

    sender ! json
    //println(json)
    // sender ! new CharacterResponse(json)
  }

  //Once characters actually have belonging we'll want to implement this and use it in getCharacterRadius
  // def getCharacterBelongings(characterId: String) = {

  // }

  //Returns the information about other entities within a room that a character might need to know
  def getEntityInfo(e: Entity): String = {
    val entityId = e.getComponent(classOf[Character])
    val entityHealth = e.getComponent(classOf[Health])
    val entityPosition = e.getComponent(classOf[Position])
    var  entityInfo = "{ \"id\": " + entityId
    entityInfo += ", \"health\": " + entityHealth.toString
    return entityInfo + ", \"position\": " + entityPosition.toString + "}"
  }

  // def getSurroundings(pos: Position) = {

  // }

  def sendMapInfo(room : Entity ) = {
    var json = "{\"type\" : \"map\", \"root\" : \"assets/maps\","
    val tilemap = room.getComponent(classOf[TileMap]) 
    json +=  "\"tilemap\": \"" + tilemap.file + "\"," 
    json +=  "\"tilesets\" : [" + getTileSetInfo(room) 
    json += "]}"
    sender ! json
  }

  def getTileSetInfo(room : Entity) : String = {
    val tileMap : TileMap = room.getComponent(classOf[TileMap])
    return tileMap.tilesets.toString
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
