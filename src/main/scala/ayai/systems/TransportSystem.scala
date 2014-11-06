package ayai.systems

/** Ayai Imports **/
import ayai.components._
import ayai.gamestate._
import ayai.apps._
import ayai.networking._
import ayai.systems.mapgenerator.ExpandRoom
import ayai.maps.TransportInfo

/** Crane Imports **/
import crane.{Entity,EntityProcessingSystem}

/** External Imports **/
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration._

/** Akka Imports **/
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.io.Source
import java.io.File

object TransportSystem {
  def apply(actorSystem: ActorSystem) = new TransportSystem(actorSystem)
}

class NoRoomFoundException(msg: String) extends RuntimeException(msg)

case class JTransport(start_x: Int, start_y: Int, end_x: Int, end_y: Int, toRoomId: Int, to_x: Int, to_y: Int, dir: String) {
  implicit def asJson(): JObject = {
    ("start_x" -> start_x) ~
    ("start_y" -> start_y) ~
    ("end_x" -> end_x) ~
    ("end_y" -> end_y) ~
    ("toRoomId" -> toRoomId) ~
    ("to_x" -> to_x) ~
    ("to_y" -> to_y) ~
    ("dir" -> dir)
  }
}

/**
** Take an entity with a Position and NetworkingActor
** Swap world of entity to designated room and send information to network actor
**/
class TransportSystem(actorSystem: ActorSystem)
extends EntityProcessingSystem(include = List(classOf[Room], classOf[Position], classOf[NetworkingActor]),
                               exclude = List(classOf[Respawn])) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
  private val log = LoggerFactory.getLogger(getClass)
  //this will only move characters who have received a movement key and the current component is still set to True
  override def processEntity(e: Entity, delta : Int) {
  (e.getComponent[Room],
    e.getComponent[Position],
    e.getComponent[NetworkingActor]) match {
      case (Some(room: Room), Some(position: Position), Some(networkingActor: NetworkingActor)) => {
        val tileMap: TileMap = world.asInstanceOf[RoomWorld].tileMap
        val transportOption: Option[TransportInfo] = tileMap.checkIfTransport(position)

        transportOption match {
          case Some(transport: TransportInfo) => {
            val roomId: Int = transport.toRoomId
            val future = actorSystem.actorSelection("user/RoomList") ? GetWorldById(roomId)
            val result = Await.result(future, timeout.duration).asInstanceOf[Option[RoomWorld]]

            val newWorld = result match {
              case Some(roomWorld: RoomWorld) => roomWorld
              case _ => throw new NoRoomFoundException("Cannot match room " + roomId)
            }

            if (newWorld.isLeaf) {
              //Generate all of its children
              val worldGenerator = actorSystem.actorSelection("user/WorldGenerator")
              worldGenerator ! new ExpandRoom(newWorld)
              newWorld.isLeaf = false
            }

            var xOffset = 0
            var yOffset = 0
            val landingDirection = transport.direction match {
              case "RightToLeft" =>
                xOffset = 1
                "LeftToRight"

              case "LeftToRight" =>
                xOffset = -1
                "RightToLeft"

              case "BottomToTop" =>
                yOffset = 1
                "TopToBottom"

              case "TopToBottom" =>
                yOffset = -1
                "BottomToTop"

              case _ =>
                println("MISSING TRANSPORT DIRECTION IN TransportSystem.")
                "RightToLeft"
            }

            val newTileMap = newWorld.tileMap
            val transportOption = newTileMap.transports.find(_.direction == landingDirection)

            //Update the room component so it will be saved to the DB on logout
            room.id = roomId

            //Right here I should look up where the next room's transport is
            transportOption match {
              case Some(t: TransportInfo) => {
                position.x = ((t.startingPosition.x + t.endingPosition.x) / 2 + xOffset) * 32
                position.y = ((t.startingPosition.y + t.endingPosition.y) / 2 + yOffset) * 32
                println("Adding character at: " + position.x + ", " + position.y)
              }
              case _ =>{
                println("Error, transport system is unable to match landingDirection.")
                position.x = transport.toPosition.x
                position.y = transport.toPosition.y
              }
            }

            val future1 = actorSystem.actorSelection("user/UserRoomMap") ? SwapWorld(e.tag, newWorld)
            Await.result(future1, timeout.duration)

            implicit val formats = net.liftweb.json.DefaultFormats
            val mapFile = Source.fromFile(new File("src/main/resources/assets/maps/" + newTileMap.file))
            val tileMapString = mapFile.mkString.filterNot({_ == "\n"})
            mapFile.close()

            val json = ("type" -> "map") ~
                        ("tilemap" -> tileMapString) ~
                        ("tilesets" -> (newWorld.tileMap.tilesets map (_.asJson)))

            val actorSelection = actorSystem.actorSelection("user/SockoSender" + e.tag)
            actorSelection ! new ConnectionWrite(compact(render(json)))
          }
          case _ =>
        }
      }

      case _ =>
        log.warn("bb12e5d: getComponent failed to get anything")
    }
  }
}
