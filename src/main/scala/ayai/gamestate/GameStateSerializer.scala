package ayai.gamestate

/** Ayai Imports **/
import ayai.components._
/** Crane Imports **/
import crane.{Entity, World}

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef}


/** External Imports **/
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.slf4j.{Logger, LoggerFactory}

sealed trait QueryType
sealed trait QueryResponse

case class CharacterRadius(characterId: String) extends QueryType
case class CharacterResponse(json: String)  extends QueryResponse
case class MapRequest(room : Entity)
case object SomeData

class GameStateSerializer(world: World, loadRadius: Int) extends Actor {
  private val log = LoggerFactory.getLogger(getClass)


  //Returns a list of entities contained within a room.
  def getRoomEntities(roomId: Long): ArrayBuffer[Entity] = {
    world.groups("ROOM" + roomId)
  }

  //Returns a character's belongings and surroundings.
  def getCharacterRadius(characterId: String) = {
    val characterEntity : Entity = (world.getEntityByTag("CHARACTER" + characterId)) match {
      case Some(entity : Entity) => 
        entity
      case _ =>
        log.warn("11491e0: getEntityByTag failed to return anything")
        new Entity
    }
    
    val room: Room = (characterEntity.getComponent(classOf[Room])) match {
      case Some(r : Room) => 
        r 
      case _ =>
        log.warn("b766a68: getComponent failed to return anything")
        new Room(-1)
    }

    val otherEntities: ArrayBuffer[Entity] = getRoomEntities(room.id)

    var entityJSONs = ArrayBuffer.empty[Entity]
    for(otherEntity <- otherEntities) {
      if(characterEntity != otherEntity) {
        otherEntity.getComponent(classOf[Character]) match {
          case(Some(a : Character)) => 
          entityJSONs += otherEntity
          case _ => 
        }
      }
    }
    
    val jsonLift: JObject = 
      ("type" -> "update") ~
      ("you" -> ((characterEntity.getComponent(classOf[Character]),
        characterEntity.getComponent(classOf[Position]),
        characterEntity.getComponent(classOf[Health]),
        characterEntity.getComponent(classOf[Inventory]),
        characterEntity.getComponent(classOf[Mana]),
        characterEntity.getComponent(classOf[Actionable]),
        characterEntity.getComponent(classOf[QuestBag]),
        characterEntity.getComponent(classOf[Equipment])) match {
          case (Some(character : Character), Some(position : Position), Some(health : Health),
           Some(inventory : Inventory), Some(mana : Mana), Some(actionable : Actionable),
           Some(questbag: QuestBag), Some(equipment: Equipment)) =>
            ((character.asJson()) ~
            (position.asJson) ~
            (health.asJson) ~
            (inventory.asJson) ~
            (mana.asJson) ~
            (actionable.action.asJson) ~
            (questbag.asJson) ~
            (equipment.asJson))
          case _ =>
            log.warn("cec6af4: getComponent failed to return anything BLARG")
            JNothing
        })) ~
       ("others" -> entityJSONs.map{ e => 
        (e.getComponent(classOf[Character]),
          e.getComponent(classOf[Position]),
          e.getComponent(classOf[Health]),
          e.getComponent(classOf[Mana]),
          e.getComponent(classOf[Actionable])) match {
          case (Some(character : Character), Some(position : Position), Some(health : Health), Some(mana : Mana), Some(actionable : Actionable)) =>
            ((character.asJson()) ~
            (position.asJson) ~
            (health.asJson) ~
            (mana.asJson) ~
            (actionable.action.asJson))
          case _ =>
            log.warn("f3d3275: getComponent failed to return anything BLARG2")
            JNothing
        }})

    try {
      sender ! compact(render(jsonLift))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        sender ! ""
    }
    // sender ! new CharacterResponse(json)
  }

  //Once characters actually have belonging we'll want to implement this and use it in getCharacterRadius
  // def getCharacterBelongings(characterId: String) = {

  // }

  // def getSurroundings(pos: Position) = {

  // }

  def sendMapInfo(room : Entity ) = {
    val json = room.getComponent(classOf[TileMap]) match {
      case (Some(tilemap : TileMap)) => 
        ("type" -> "map") ~
        ("tilemap" -> tilemap.file) ~
        (tilemap.tilesets.asJson)
      case _ =>
        log.warn("eeec172: getComponent failed to return anything")
        JNothing
    } 

    try {
      println(compact(render(json)))
      sender ! compact(render(json))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        sender ! ""
    }
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
