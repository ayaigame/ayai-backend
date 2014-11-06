package ayai.gamestate

/** Akka Imports **/
import akka.actor.Actor

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

case class AddWorld(world: RoomWorld)
case class GetAllWorlds()
case class GetWorldById(id: Int)
case class GetWorldsByIds(ids: List[Int])
case class GetTransportsToRoom(id: Int)

class RoomList extends Actor {
  private val roomList: ArrayBuffer[RoomWorld] = ArrayBuffer[RoomWorld]()

  def receive = {
    case AddWorld(world: RoomWorld) => roomList += world
    case GetAllWorlds() => sender ! roomList
    case GetWorldById(id: Int) => sender ! roomList.find(_.id == id)
    case GetWorldsByIds(ids: List[Int]) => sender ! roomList.toList.filter(item => ids.contains(item.id))
    case GetTransportsToRoom(id: Int) => {
      val allTransports = roomList.toList.flatMap(_.tileMap.transports)
      sender ! allTransports.filter(_.toRoomId == id)
    }
  }
}
