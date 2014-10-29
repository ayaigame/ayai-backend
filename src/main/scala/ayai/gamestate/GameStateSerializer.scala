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
  // TODO use Option[T]
  private val log = LoggerFactory.getLogger(getClass)
  private var roomJSON: JObject = null
  private var valid: Boolean = false
  private var npcJSON: JObject = null
  private var projectilesJSON: JObject = null
  private var lootJSON: JObject = null

  //Returns a list of entities contained within a room.
  def getRoomEntities(roomId: Long): ArrayBuffer[Entity] = {
    world.groups("ROOM" + roomId)
  }

  //Returns a character's belongings and surroundings.
  def getRoom(e: Entity) = {
    if(!valid) {
      val entities = world.getEntitiesWithExclusions(include=List(classOf[Character], classOf[Position],
                                                      classOf[Health], classOf[Mana], classOf[SpriteSheet]
                                                      , classOf[Experience]),
                                                     exclude=List(classOf[NPC], classOf[Dead]))
        val jsonLift: JObject =
            ("players" -> entities.map{ e =>
             (e.getComponent(classOf[Character]),
               e.getComponent(classOf[Position]),
               e.getComponent(classOf[Health]),
               e.getComponent(classOf[Mana]),
               e.getComponent(classOf[Actionable]),
               e.getComponent(classOf[SpriteSheet]),
               e.getComponent(classOf[Experience])) match {
                 case (Some(character: Character), Some(position: Position), Some(health: Health),
                       Some(mana: Mana), Some(actionable: Actionable), Some(spritesheet: SpriteSheet),
                       Some(experience: Experience)) =>
                   ((character.asJson) ~
                   (position.asJson) ~
                   (health.asJson) ~
                   (mana.asJson) ~
                   (actionable.action.asJson) ~
                   (spritesheet.asJson) ~
                   (experience.asJson))
                 case _ =>
                   log.warn("f3d3275: getComponent failed to return anything BLARG2")
                   JNothing
             }})
        val npcs = world.getEntitiesWithExclusions(include=List(classOf[Character], classOf[Position],
                                                 classOf[Health], classOf[Mana],
                                                 classOf[NPC], classOf[SpriteSheet]),
                                                  exclude=List(classOf[Dead]))

        npcJSON= ("npcs" -> npcs.map{ npc =>
          (npc.getComponent(classOf[Character]),
            npc.getComponent(classOf[Position]),
            npc.getComponent(classOf[Health]),
            npc.getComponent(classOf[Mana]),
            npc.getComponent(classOf[SpriteSheet])) match {
              case (Some(character: Character), Some(position: Position),
                Some(health: Health), Some(mana: Mana), Some(spritesheet: SpriteSheet)) =>
                ((character.asJson) ~
                  (position.asJson) ~
                  (health.asJson) ~
                  (mana.asJson) ~
                  (spritesheet.asJson))
              case _ =>
                log.warn("f3d3275: getComponent failed to return anything BLARG2")
                JNothing
            }})

        val loot = world.getEntitiesWithExclusions(include=List(classOf[Loot]))

        lootJSON = ("loot" -> loot.map{ l => 
          (l.getComponent(classOf[Position]),
            l.getComponent(classOf[SpriteSheet]),
            l.getComponent(classOf[Loot])) match {
              case (Some(position: Position), Some(spritesheet: SpriteSheet)
                    , Some(lo: Loot)) => 
                (position.asJson) ~
                (spritesheet.asJson) ~
                (lo.asJson)
              case _ => 
                log.warn("f3d3275: getComponent failed to return anything BLARG2")
                JNothing
            }})        

        val projectiles = world.getEntitiesWithExclusions(include=List(classOf[Projectile], classOf[Position], classOf[SpriteSheet]),
                                                  exclude=List(classOf[Dead]))

        projectilesJSON = ("projs" -> projectiles.map{ projectile =>
          (projectile.getComponent(classOf[Projectile]),
            projectile.getComponent(classOf[Position]),
            projectile.getComponent(classOf[SpriteSheet])) match {
              case (Some(projectileComponent: Projectile),
                    Some(position: Position),
                    Some(spritesheet: SpriteSheet)) =>
                ((projectileComponent.asJson) ~
                  (position.asJson) ~
                  (spritesheet.asJson))
              case _ =>
                log.warn("f3d3275: getComponent failed to return anything BLARG2")
                JNothing
            }})
            // println(compact(render(npcJSON)))
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
    sender ! compact(render(("type" -> "update")~(roomJSON)~(npcJSON)~(projectilesJSON)~(lootJSON)~(getCharacterAssets(e))))
  }

  def getCharacterAssets(entity: Entity): JObject = {
    val jsonLift = (entity.getComponent(classOf[Inventory]),
      entity.getComponent(classOf[Equipment]),
      entity.getComponent(classOf[QuestBag]),
      entity.getComponent(classOf[Loot]),
      entity.getComponent(classOf[Stats])) match {
        case (Some(inventory: Inventory), Some(equipment: Equipment),
              Some(quests: QuestBag), None, Some(stats: Stats)) =>
          (inventory.asJson) ~
          (equipment.asJson) ~
          (quests.asJson) ~
          (stats.asJson)
        case (Some(inventory: Inventory), None, None, Some(loot: Loot), None) =>
          (inventory.asJson) ~
          (loot.asJson)
        case _ => JNothing
      }
    ("models" -> jsonLift)

  }

  // TODO
  // Once characters actually have belonging we'll want to implement this and use it in getCharacterRadius
  // def getCharacterBelongings(characterId: String) = {

  // }

  // def getSurroundings(pos: Position) = {

  // }

  def sendMapInfo(room: Entity ) = {
    val tileMap = world.asInstanceOf[RoomWorld].tileMap
    val json = ("type" -> "map") ~
               ("tilemap" -> tileMap.file) ~
               ("tilesets" -> (tileMap.tilesets map(_.asJson)))

    try {
      sender ! compact(render(json))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        sender ! ""
      }
    }
  }

  def receive = {
    case GetRoomJson(e: Entity) => getRoom(e)
    case MapRequest(room) => sendMapInfo(room)
    case Refresh => valid = false
    case _ => println("Error: from serializer.")
  }
}
