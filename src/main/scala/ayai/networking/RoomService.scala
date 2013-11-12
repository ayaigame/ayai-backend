package ayai.main.networking

import ayai.main.gamestate._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._
import scala.util.parsing.json._

class RoomService(connection: Connection) extends Service(connection) {
  def serve = {
    while(connection.isConnected()) {
      var request = connection.read()
      if(request.length > 0) {
        val result = JSON.parseFull(request)
        val playerId: Int = result match {
          case Some(e: Map[String, Double]) => e("player_id").toInt
          case _ => {
            println("Failed player request.")
            -1
          }
        }
        connection.write(GameState.getPlayerRoomJSON(playerId))
      }
    }
    connection.kill()
  }

  def receive = {
    case StartConnection() => serve
    case _ => println("Error: incomprehensible message.")
  }
}
