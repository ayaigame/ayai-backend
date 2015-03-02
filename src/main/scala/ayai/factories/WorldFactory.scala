package ayai.factories

/** Ayai Imports **/
import ayai.systems._
import ayai.gamestate.{RoomWorld, GameStateSerializer, MessageProcessorSupervisor}

/** External Imports **/
import net.liftweb.json._

/** Akka Imports **/
import akka.actor.{Actor, Props}
import akka.actor.Status.Failure

import scala.io.Source
import java.io.File

case class CreateWorld(fileName: String)

class WorldFactory extends Actor {

  def receive = {
      /**
      ** Create a world and instantiate all needed systems and create message processors
      **/
    case CreateWorld(jsonFile: String) => {
      implicit val formats = net.liftweb.json.DefaultFormats
      val file = Source.fromFile(new File("src/main/resources/assets/maps/" + jsonFile))

      val lines = file.mkString
      file.close()

      val parsedJson = parse(lines)
      val tileMap = EntityFactory.loadRoomFromJson(jsonFile, parsedJson)
      val id = (parsedJson \ "id").extract[Int]

      val world = RoomWorld(id, tileMap, isLeaf = true)
      world.addSystem(MovementSystem(), 1)
      world.addSystem(TransportSystem(context.system), 2)
      world.addSystem(HealthSystem())
      world.addSystem(RespawningSystem(context.system))
      world.addSystem(FrameExpirationSystem(context.system))
      world.addSystem(StatusEffectSystem())
      world.addSystem(ItemSystem())
      world.addSystem(StatusEffectSystem())
      world.addSystem(LevelingSystem(context.system))
      // world.addSystem(DirectorSystem(context.system))
      world.addSystem(NPCRespawningSystem())
      world.addSystem(GoalSystem(context.system))
      world.addSystem(RoomChangingSystem(context.system))
      world.addSystem(NetworkingSystem(context.system), 3)
      world.addSystem(CollisionSystem(context.system))
      world.addSystem(AttackSystem(context.system))
      world.addSystem(CooldownSystem(context.system))

      world.addSystem(QuestGenerationSystem(context.system))
      world.addSystem(PerceptionSystem(context.system))
      world.addSystem(VisionSystem(context.system))
      world.addSystem(SoundSystem(context.system))
      world.addSystem(MemorySystem(context.system))
      world.addSystem(CommunicationSystem(context.system))

      val serializer = context.system.actorOf(Props(GameStateSerializer(world)), s"Serializer$id")
      val nmProcessor = context.system.actorOf(Props(MessageProcessorSupervisor(world)), name=s"MProcessor$id")

      //HARDCODED ENTITY ADD HAPPENS HERE
      // val entity = EntityFactory.createAI(world, "axis")
      // world.addEntity(entity)

      sender ! world
    }
    case _ => {
      println("Error: from WorldFactory.")
      sender ! Failure
    }
  }
}
