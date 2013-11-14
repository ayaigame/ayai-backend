package ayai.networking

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorRef

import scala.util.parsing.json._

class NetworkMessageInterpreter(queue: ActorRef) extends Actor {
  def interpretMessage(connectionId: Int, message: String) = {
        val result = JSON.parseFull(message)
        val playerId: Int = result match {
          case Some(e: Map[String, Double]) => e("player_id").toInt
          case _ => {
            println("Failed player request.")
            -1
          }
        }

        queue ! AddInterpretedMessage(new PlayerRequest(connectionId, playerId))
  }

  def receive = {
    case InterpretMessage(connectionId, message) => interpretMessage(connectionId, message)
    case _ => println("Error: from interpreter.")
  }
}