package ayai.networking

import ayai.gamestate._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._

class ConnectionReader(connection: Connection, interpreter: ActorRef) extends Service(connection) {
  def acceptMessages = {
    while(connection.isConnected()) {
      var request = connection.read()
      if(request.length > 0) {
        interpreter ! new InterpretMessage(connection.getId, request)
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
