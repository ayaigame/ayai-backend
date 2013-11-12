package ayai.main.networking.messaging

import ayai.main.gamestate._
import ayai.main.networking.Connection

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._
import scala.util.parsing.json._

class MessagingService(connection: Connection) extends Actor {

  def receive = {
    // Need to think of better way to "case" messages.

    
   	case PrivateMessage(m: String, s: String, r: String, a: ActorRef) =>
      val priMsg = new PrivateMessage(m, s, r, this.asInstanceOf[ActorRef])
      priMsg.send

 	case PublicMessage(m: String, s: String) =>
      val pubMsg = new PublicMessage(m, s)
      pubMsg.send

    case ReceiveMessage(m: String, s: String) =>
      println("Received: " + m)

    case _ => println("Unknown message type")
   }
  
}
