package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

import scala.util.parsing.json._

class NetworkMessageInterpreter extends Actor {
  def interpretMessage(message: String) = {
        val result = JSON.parseFull(message)
        val playerId: Int = result match {
          case Some(e: Map[String, Double]) => e("player_id").toInt
          case _ => {
            println("Failed player request.")
            -1
          }
        }
        println(playerId)

        sender ! AddInterpretedMessage(new PlayerRequest(playerId))
  }

  def receive = {
    case InterpretMessage(message) => interpretMessage(message)
    case _ => println("Error: from interpreter.")
  }
}