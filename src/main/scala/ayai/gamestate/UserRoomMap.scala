package ayai.gamestate

import crane.Entity

import akka.actor.Actor
import akka.actor.Status.{Success, Failure}
import scala.collection.mutable.HashMap

case class AddAssociation(id: String, world: RoomWorld)
case class RemoveUserRoom(id: String)
case class GetWorld(id: String)
case class SwapWorld(id: String, to: RoomWorld)

class UserRoomMap extends Actor {
  val userRoomMap: HashMap[String, RoomWorld] = HashMap[String, RoomWorld]()

  def receive = {
    case AddAssociation(id: String, world: RoomWorld) => userRoomMap(id) = world
    case RemoveUserRoom(id: String) => userRoomMap -= id
    case GetWorld(id: String) => sender ! userRoomMap(id)
    case SwapWorld(id: String, to: RoomWorld) =>
      userRoomMap(id).getEntityByTag(id) match {
        case Some(e: Entity) =>
          to.addEntity(e.copy())
          userRoomMap(id).removeEntity(e)
          userRoomMap(id) = to
          sender ! Success
        case _ =>
          println("Error in UserMap Room")
          sender ! Failure
      }
  }

}
