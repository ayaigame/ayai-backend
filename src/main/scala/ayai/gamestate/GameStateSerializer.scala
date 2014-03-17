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
case class MapRequest(room: Entity)
case class GetRoomJson(e: Entity)
case object Refresh
case object SomeData

object GameStateSerializer {
  def apply(world: World) = new GameStateSerializer(world)
}

class GameStateSerializer(world: World) extends Actor {
  private val log = LoggerFactory.getLogger(getClass)
  private var roomJSON: JObject = null
  private var valid: Boolean = false

  //Returns a list of entities contained within a room.
  def getRoomEntities(roomId: Long): ArrayBuffer[Entity] = {
    world.groups("ROOM" + roomId)
  }

  //Returns a character's belongings and surroundings.
  def getRoom(e: Entity) = {
    if(!valid) {
      var entities = world.getEntitiesByComponents(classOf[Character], classOf[Position],
                                                      classOf[Health], classOf[Mana])
         val jsonLift: JObject =
            ("players" -> entities.map{ e =>
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
            var npcs = world.getEntitiesByComponents(classOf[Interactable], classOf[Character], classOf[Position],
                                                     classOf[Health], classOf[Mana])
            jsonLift += ("npcs" -> npcs.map { npc =>
              (npc.getComponent(classOf[Interactable]),
                npc.getComponent(classOf[Character]),
                npc.getComponent(classOf[Position]),
                npc.getComponent(classOf[Health]),
                npc.getComponent(classOf[Mana]))}) match {
                  case (Some(interact: Interact), Some(character: Character), Some(position: Position),
                    Some(health: Health), Some(mana: Mana)) =>
                    ((interact.asJson) ~
                      (character.asJson) ~
                      (position.asJson) ~
                      (health.asJson) ~
                      (mana.asJson))
                  case _ =>  
                    log.warn("f3d3275: getComponent failed to return anything BLARG2")
                    JNothing
                }
                
         try {
           roomJSON = jsonLift
           valid = true
         } catch {
           case e: Exception =>
             e.printStackTrace()
             sender ! ""
         }

    }
    // println(compact(render(getCharacterAssets(e))))
    sender ! compact(render(("type" -> "update")~(roomJSON)~(getCharacterAssets(e))))
  }

  def getCharacterAssets(entity: Entity): JObject = {
    val jsonLift = (entity.getComponent(classOf[Inventory]),
      entity.getComponent(classOf[Equipment]),
      entity.getComponent(classOf[QuestBag]),
      entity.getComponent(classOf[Loot])) match {
        case (Some(inventory: Inventory), Some(equipment: Equipment), Some(quests: QuestBag), None) => 
          (inventory.asJson) ~
          (equipment.asJson) ~
          (quests.asJson)
        case (Some(inventory: Inventory), None, None, Some(loot: Loot)) =>
          (inventory.asJson) ~
          (loot.asJson)
        case _ => JNothing
      }
    ("models" -> jsonLift)

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
      sender ! compact(render(json))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        sender ! ""
    }
  }

  def receive = {
    case GetRoomJson(e: Entity) => getRoom(e)
    case MapRequest(room) => sendMapInfo(room)
    case Refresh => valid = false
    case _ => println("Error: from serializer.")
  }
}
