package ayai.gamestate

import akka.actor.Actor

import scala.collection.mutable.ArrayBuffer

case class AddWorld(world: RoomWorld)
case class GetWorldByName(name: String)

class RoomList extends Actor {
  val roomList: ArrayBuffer[RoomWorld] = ArrayBuffer[RoomWorld]()

  def receive = {
    case AddWorld(world: RoomWorld) =>
      roomList += world
    case GetWorldByName(name: String) =>
      sender ! roomList.find(r => r.name == name)
  }

}
