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
case class GetRoomJson
case class MapRequest(room: Entity)
case object SomeData

class GameStateSerializer(world: World) extends Actor {
  private val log = LoggerFactory.getLogger(getClass)

  //Returns a list of entities contained within a room.
  def getRoomEntities(roomId: Long): ArrayBuffer[Entity] = {
    world.groups("ROOM" + roomId)
  }

  //Returns a character's belongings and surroundings.
  def getRoom = {
    var entityJSON = world.getEntityByComponents(classOf[Character], classOf[Position],
                                                 classOf[Health], classOf[Mana],
                                                 classOf[Actionable])
    val jsonLift: JObject =
      ("type" -> "update") ~
       ("players" -> entityJSON.map{ e =>
        (e.getComponent(classOf[Character]),
          e.getComponent(classOf[Position]),
          e.getComponent(classOf[Health]),
          e.getComponent(classOf[Mana]),
          e.getComponent(classOf[Actionable])) match {
            case (Some(character: Character), Some(position: Position), Some(health: Health),
                  Some(mana: Mana), Some(actionable: Actionable)) =>
              ((character.asJson) ~
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

  def sendMapInfo(room: Entity ) = {
    val tileMap = world.asInstanceOf[RoomWorld].tileMap
    val json =  ("type" -> "map") ~
                ("tilemap" -> tileMap.file) ~
                (tileMap.tilesets.asJson)

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
    case GetRoomJson=> getRoom
    case MapRequest(room) => sendMapInfo(room)
    case _ => println("Error: from serializer.")
  }
}
