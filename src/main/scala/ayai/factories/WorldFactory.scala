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

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

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

      var world: RoomWorld = RoomWorld(id, tileMap, true)
      world.addSystem(MovementSystem(), 1)
      world.addSystem(TransportSystem(context.system), 2)
      world.addSystem(HealthSystem())
      world.addSystem(RespawningSystem(context.system))
      world.addSystem(FrameExpirationSystem(context.system))
      world.addSystem(StatusEffectSystem())
      world.addSystem(ItemSystem())
      world.addSystem(StatusEffectSystem())
      world.addSystem(LevelingSystem(context.system))
      // world.addSystem(DirectorSystem())
      world.addSystem(NPCRespawningSystem())
      world.addSystem(GoalSystem(context.system))
      world.addSystem(RoomChangingSystem(context.system))
      world.addSystem(NetworkingSystem(context.system), 3)
      world.addSystem(CollisionSystem(context.system))
      world.addSystem(AttackSystem(context.system))
      world.addSystem(CooldownSystem(context.system))
      val serializer = context.system.actorOf(Props(GameStateSerializer(world)), s"Serializer$id")
      val nmProcessor = context.system.actorOf(Props(MessageProcessorSupervisor(world)), name=s"MProcessor$id")

      //HARDCODED ENTITY ADD HAPPENS HERE
      // val entity = EntityFactory.createAI(world, "axis")
      // world.addEntity(entity)

      sender ! world
    }
    case _ => println("Error: from WorldFactory.")
      sender ! Failure
  }
}
