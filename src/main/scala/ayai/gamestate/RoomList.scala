package ayai.gamestate

import ayai.maps.TransportInfo

/** Akka Imports **/
import akka.actor.{Actor, ActorRef}
import akka.util.Timeout

/** External Imports **/
import scala.concurrent.{Await, ExecutionContext, Promise, Future}
import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer

case class AddWorld(world: RoomWorld)
case class GetAllWorlds()
case class GetWorldById(id: Int)
case class GetWorldsByIds(ids: List[Int])
case class GetTransportsToRoom(id: Int)

class RoomList extends Actor {
  val roomList: ArrayBuffer[RoomWorld] = ArrayBuffer[RoomWorld]()

  def receive = {
    case AddWorld(world: RoomWorld) =>
      roomList += world
    case GetAllWorlds() =>
      sender ! roomList
    case GetWorldById(id: Int) =>
      sender ! roomList.find(r => r.id == id)
    case GetWorldsByIds(ids: List[Int]) =>
      sender ! roomList.toList.filter(item => ids.contains(item.id))
    case GetTransportsToRoom(id: Int) => {
      val allTransports: List[List[TransportInfo]] = roomList.toList map (_.tileMap.transports)
      sender ! (allTransports.flatten filter (_.toRoomId == id))
    }
  }

}
