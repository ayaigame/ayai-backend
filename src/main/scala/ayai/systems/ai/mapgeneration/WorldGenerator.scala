package ayai.systems.mapgenerator

import scala.concurrent.Await
import scala.concurrent.duration._

import ayai.gamestate.{RoomWorld, RoomList, AddWorld, GetWorldsByIds}
import ayai.apps.Constants

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

/** External Imports **/
import scala.concurrent.{Await, ExecutionContext, Promise, Future}
import scala.concurrent.duration._

case class ExpandRoom(room: RoomWorld)

class WorldGenerator() extends Actor {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
  val mapGenerator = context.system.actorOf(Props[MapGenerator], name="MapGenerator")

  def _getRoomsToBuild(room: RoomWorld): Set[Int] = {
    // println("I'm supposed to be expanding room: " + room.id)
    val transports = room.tileMap.listOfTransport

    val childrenIds = transports map (_.toRoomId)
    // println("Whose children are: " + childrenIds.toString)

    var future = context.system.actorSelection("user/RoomList") ? new GetWorldsByIds(childrenIds)
    val existingRooms = Await.result(future, timeout.duration).asInstanceOf[List[RoomWorld]]

    val existingRoomIds = existingRooms map (_.id)
    // println("These rooms already exist:" + existingRoomIds.toString)

    childrenIds.toSet -- existingRoomIds
  }

  def buildRoom(id: Int) = {
    val future = mapGenerator ? new CreateMap(id, 50, 50)

    context.system.actorSelection("user/RoomList") ! new AddWorld(Await.result(future, timeout.duration).asInstanceOf[RoomWorld])

  }

  def receive = {
    case ExpandRoom(room: RoomWorld) => {
      val roomsToBuild = _getRoomsToBuild(room)
      // println("I need to build these rooms: " + roomsToBuild.toString)

      roomsToBuild map buildRoom
    }

    case _ => println("Error: from WorldGenerator.")
      sender ! Failure
  }
}