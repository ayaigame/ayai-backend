package ayai.networking

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/** Socko Imports **/
import org.mashupbots.socko.routes._
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import scala.util.parsing.json._
import net.liftweb.json._

import java.rmi.server.UID

class NetworkMessageInterpreter(queue: ActorRef) extends Actor {
  def interpretMessage(wsFrame: WebSocketFrameEvent) = {
    val rootJSON = parse(wsFrame.readText)
    val tempType:String = compact(render(rootJSON \ "type"))
    val msgType:String = tempType.substring(1, tempType.length - 1)

    msgType match {
      case "init" =>
        context.system.actorOf(Props(new SockoSender(wsFrame)), "SockoSender"+ (new UID()).toString)
      case "echo" =>
        queue ! new AddInterpretedMessage(new NetworkMessage("echo"))
      case _ =>
        println("Unknown message in NetworkMessageInterpreter: " + msgType)
    }
  }

  def receive = {
    case InterpretMessage(message) => interpretMessage(message)
    case _ => println("Error: from interpreter.")
  }
}