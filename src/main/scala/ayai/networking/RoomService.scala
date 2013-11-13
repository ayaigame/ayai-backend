package ayai.networking

import ayai.gamestate._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._

class RoomService(connection: Connection, queue: ActorRef) extends Service(connection) {
  def acceptMessages = {
    while(connection.isConnected()) {
      var request = connection.read()
      if(request.length > 0) {
        var message = new AddRawMessage(request)
        queue ! message
      }
    }
    connection.kill()
  }
        // connection.write(GameState.getPlayerRoomJSON(playerId))

  def receive = {
    case StartConnection() => acceptMessages
    case _ => println("Error: incomprehensible message.")
  }
}
