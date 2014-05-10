package ayai.factories

/** Ayai Imports **/
import ayai.apps.Constants
import ayai.systems._
import ayai.gamestate.{RoomWorld, GameStateSerializer, MessageProcessorSupervisor, TileMap}

/** Akka Imports **/
import akka.actor.{ActorSystem, Props}

/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.io.Source

object WorldFactory {
  def apply(networkSystem: ActorSystem) = new WorldFactory(networkSystem)
}

class WorldFactory(networkSystem: ActorSystem) {
  /**
  ** Create a world and instantiate all needed systems and create message processors
  **/
  def createWorld(fileName: String): RoomWorld = {
    val jsonFile: String = s"$fileName.json"

    implicit val formats = net.liftweb.json.DefaultFormats
    val file = Source.fromURL(getClass.getResource("/assets/maps/" + jsonFile))
    val lines = file.mkString
    file.close()

    val parsedJson = parse(lines)

    val tileMap = EntityFactory.loadRoomFromJson(jsonFile, parsedJson)

    val id = (parsedJson \ "id").extract[Int]
    var world: RoomWorld = RoomWorld(id, tileMap, true)

    world.addSystem(MovementSystem(), 1)
    world.addSystem(TransportSystem(networkSystem), 2)
    world.addSystem(HealthSystem())
    world.addSystem(RespawningSystem())
    world.addSystem(FrameExpirationSystem())
    world.addSystem(StatusEffectSystem())
    world.addSystem(ItemSystem())
    world.addSystem(StatusEffectSystem())
    world.addSystem(LevelingSystem(networkSystem))
    // world.addSystem(DirectorSystem())
    world.addSystem(NPCRespawningSystem())
    world.addSystem(GoalSystem(networkSystem))
    world.addSystem(RoomChangingSystem(networkSystem))
    world.addSystem(NetworkingSystem(networkSystem), 3)
    world.addSystem(CollisionSystem(networkSystem))
    world.addSystem(AttackSystem(networkSystem))
    world.addSystem(CooldownSystem(networkSystem))
    val serializer = networkSystem.actorOf(Props(GameStateSerializer(world)), s"Serializer$id")
    val nmProcessor = networkSystem.actorOf(Props(MessageProcessorSupervisor(world)), name=s"MProcessor$id")

    //HARDCODED ENTITY ADD HAPPENS HERE
    val entity = EntityFactory.createAI(world, "axis")
    world.addEntity(entity)

    world
  }
}
