package ayai.networking

import ayai.gamestate._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._
import scala.util.parsing.json._

class RoomService(connection: Connection, queue: ActorRef) extends Service(connection) {
  def acceptMessages = {
    while(connection.isConnected()) {
      var request = connection.read()
      if(request.length > 0) {
        var message = new AddMessage(request)
        queue ! message
        // val result = JSON.parseFull(request)
        // val playerId: Int = result match {
        //   case Some(e: Map[String, Double]) => e("player_id").toInt
        //   case _ => {
        //     println("Failed player request.")
        //     -1
        //   }
        // }
        // connection.write(GameState.getPlayerRoomJSON(playerId))
      }
    }
    connection.kill()
  }

  def receive = {
    case StartConnection() => acceptMessages
    case _ => println("Error: incomprehensible message.")
  }
}
