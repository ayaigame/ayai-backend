package ayai.systems.mapgenerator

import ayai.gamestate.RoomWorld

/** Akka Imports **/
import akka.actor.{Actor, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

case class ExpandRoom(room: RoomWorld)

class WorldGenerator() extends Actor {
  def receive = {
    case ExpandRoom(room: RoomWorld) => {
      println("I'm supposed to be expanding room: " + room.name)
      val transports = room.tileMap.listOfTransport

      val children = transports map (_.toRoomId)
      println("Whose children are: " + children.toString)

      roomList = context.system.actorOf(Props[RoomList])
    }

    case _ => println("Error: from WorldGenerator.")
      sender ! Failure
  }
}