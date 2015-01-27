package ayai.systems

/** Ayai Imports **/
import ayai.apps.Constants
import ayai.networking._
import ayai.components._
import ayai.persistence._
import ayai.gamestate.{GameStateSerializer, MapRequest, RoomWorld, GetRoomJson, Refresh}
import ayai.factories._

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

/** Crane Imports **/
import crane.{Entity, World, TimedSystem}

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.concurrent.duration._
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import scala.io.Source

import java.rmi.server.UID
import java.lang.Boolean

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import org.slf4j.{Logger, LoggerFactory}

object NetworkingSystem {
  def apply(networkSystem: ActorSystem) = new NetworkingSystem(networkSystem)
}

/**
** Get all entities which contain a NetworkingActor and write the update message to their connection
** Run every 30 seconds
**/
class NetworkingSystem(networkSystem: ActorSystem) extends TimedSystem(1000/30) {
  private val log = LoggerFactory.getLogger(getClass)
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  override def processTime(delta: Int) {
    val worldId = world.asInstanceOf[RoomWorld].id
    val serializer = networkSystem.actorSelection(s"user/Serializer$worldId")

    val entities = world.getEntitiesByComponents(classOf[Character], classOf[NetworkingActor])

    for(characterEntity <- entities) {
      val characterId = characterEntity.getComponent(classOf[Character]) match {
        case Some(c: Character) => c.id
        case _ =>
          log.warn("8192c19: getComponent failed to return anything")
          ""
      }

      //This is how we get character specific info, once we actually integrate this in.
      val future = serializer ? GetRoomJson(characterEntity)
      val result = Await.result(future, timeout.duration).asInstanceOf[String]
      characterEntity.getComponent(classOf[NetworkingActor]) match {
        case Some(na : NetworkingActor) => na.actor ! new ConnectionWrite(result)
        case _ =>
      }
    }
    serializer ! Refresh
  }

  def broadcastMessage(message : String) {
    val actorSelection = networkSystem.actorSelection("user/SockoSender*")
    actorSelection ! new ConnectionWrite(message)
  }
}
