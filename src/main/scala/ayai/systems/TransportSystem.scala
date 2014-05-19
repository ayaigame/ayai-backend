package ayai.systems

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.gamestate._
import ayai.apps._
import ayai.networking._
import ayai.systems.mapgenerator.{WorldGenerator, ExpandRoom}
/** Crane Imports **/
import crane.{Entity,EntityProcessingSystem}

/** External Imports **/
import scala.collection.mutable.HashMap
import org.slf4j.{Logger, LoggerFactory}
import java.util.Map
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.concurrent.duration._

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.io.Source

object TransportSystem {
  def apply(actorSystem: ActorSystem) = new TransportSystem(actorSystem)
}

class NoRoomFoundException(msg: String) extends RuntimeException(msg)


/**
** Take an entity with a Position and NetworkingActor
** Swap world of entity to designated room and send information to network actor
**/
class TransportSystem(actorSystem: ActorSystem)
extends EntityProcessingSystem(include=List(classOf[Room],
                                            classOf[Position],
                                            classOf[NetworkingActor]),
                               exclude=List(classOf[Respawn])) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
  private val log = LoggerFactory.getLogger(getClass)
  //this will only move characters who have received a movement key and the current component is still set to True
  override def processEntity(e: Entity, delta : Int) {
  (e.getComponent(classOf[Room]),
    e.getComponent(classOf[Position]),
    e.getComponent(classOf[NetworkingActor])) match {
      case (Some(room: Room), Some(position: Position), Some(networkingActor: NetworkingActor)) => {
        val tileMap: TileMap = world.asInstanceOf[RoomWorld].tileMap
        val roomId: Int = tileMap.checkIfTransport(position)

        if(roomId >= 0) {
          val future = actorSystem.actorSelection("user/RoomList") ? GetWorldById(roomId)
          val result = Await.result(future, timeout.duration).asInstanceOf[Option[RoomWorld]]

          val newWorld = result match {
            case Some(roomWorld: RoomWorld) => roomWorld
            case _ => throw new NoRoomFoundException("Cannot match room " + roomId)
          }

          if(newWorld.isLeaf) {
            //Generate all of its children
            val worldGenerator = actorSystem.actorSelection("user/WorldGenerator")
            worldGenerator ! new ExpandRoom(newWorld)
          }

          val future1 = actorSystem.actorSelection("user/UserRoomMap") ? SwapWorld(e.tag, newWorld)
          Await.result(future1, timeout.duration)

          //Update the room component so it will be saved to the DB on logout
          room.id = roomId

          val newTileMap = newWorld.tileMap

          position.x = 100
          position.y = 100

          implicit val formats = net.liftweb.json.DefaultFormats
          val mapFile = Source.fromURL(getClass.getResource("/assets/maps/" + newTileMap.file))
          val tileMapString = mapFile.mkString.filterNot({_ == "\n"})
          mapFile.close()

          val json = ("type" -> "map") ~
                      ("tilemap" -> tileMapString) ~
                      ("tilesets" -> (newWorld.tileMap.tilesets map (_.asJson)))

          println(compact(render(json)))
          val actorSelection = actorSystem.actorSelection("user/SockoSender" + e.tag)
          actorSelection ! new ConnectionWrite(compact(render(json)))
        }
      }

      case _ =>
        log.warn("bb12e5d: getComponent failed to get anything")
    }
  }
}
