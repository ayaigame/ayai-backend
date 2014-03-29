package ayai.systems

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.gamestate._
import ayai.apps._
import ayai.networking._
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

object TransportSystem {
  def apply(actorSystem: ActorSystem) = new TransportSystem(actorSystem)
}

/**
** Take an entity with a Position and NetworkingActor
** Swap world of entity to designated room and send information to network actor
**/
class TransportSystem(actorSystem: ActorSystem) 
extends EntityProcessingSystem(include=List(classOf[Position], 
                                            classOf[NetworkingActor]),
                               exclude=List(classOf[Respawn])) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
  private val log = LoggerFactory.getLogger(getClass)
  //this will only move characters who have received a movement key and the current component is still set to True
  override def processEntity(e: Entity, delta : Int) {
  (e.getComponent(classOf[Position]),
    e.getComponent(classOf[NetworkingActor])) match {
    case (Some(position: Position), Some(networkingActor: NetworkingActor)) =>
        
      val tileMap: TileMap = world.asInstanceOf[RoomWorld].tileMap
      val roomId: String = tileMap.checkIfTransport(position)
      if(roomId != "") {
        position.x = 100
        position.y = 100
        val future = actorSystem.actorSelection("user/RoomList") ? GetWorldByName("room"+roomId)
        val result = Await.result(future, timeout.duration).asInstanceOf[Option[RoomWorld]]
        val world = result match {
          case Some(roomWorld: RoomWorld) => roomWorld 
        }
        val future1 = actorSystem.actorSelection("user/UserRoomMap") ? SwapWorld(e.tag, world)
        Await.result(future1, timeout.duration)
        val tileMap = world.tileMap
        val json = ("type" -> "map") ~
                    ("tilemap" -> tileMap.file) ~
                    (tileMap.tilesets.asJson)
        val actorSelection = actorSystem.actorSelection("user/SockoSender" + e.tag)
        actorSelection ! new ConnectionWrite(compact(render(json)))
      }
      case _ =>
        log.warn("bb12e5d: getComponent failed to get anything")
    }
  }
}
