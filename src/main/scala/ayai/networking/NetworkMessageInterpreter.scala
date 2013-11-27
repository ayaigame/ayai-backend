package ayai.networking

/** Ayai Imports **/
import ayai.actions._

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
        val id = (new UID()).toString
        context.system.actorOf(Props(new SockoSender(wsFrame)), "SockoSender" + id)
        wsFrame.writeText("{\"type\": \"id\", \"id\": \"" + id + "\"}")
        queue ! new AddInterpretedMessage(new AddNewPlayer(id))
        queue ! new AddInterpretedMessage(new SocketPlayerMap(wsFrame, id))
      case "echo" =>
        queue ! new AddInterpretedMessage(new JSONMessage("echo"))
      case "move" =>
        //TODO: Add exceptions and maybe parse shit a bit more intelligently
        val tempId:String = compact(render(rootJSON \ "id"))
        val id:String = tempId.substring(1, tempId.length - 1)        
        val start:Boolean = compact(render(rootJSON \ "start")).toBoolean
        val dir:Int = compact(render(rootJSON \ "dir")).toInt
        val direction: MoveDirection = dir match {
          case 0 => new UpDirection
          case 1 => new UpRightDirection
          case 2 => new RightDirection
          case 3 => new DownRightDirection
          case 4 => new DownDirection
          case 5 => new DownLeftDirection
          case 6 => new LeftDirection
          case 7 => new UpLeftDirection
          case _ => { 
            println("Direction not found, in Interpreter")
            new MoveDirection(0, 0)
          }
        } 
        queue ! new AddInterpretedMessage(new MoveMessage(wsFrame, start, direction))
      case _ =>
        println("Unknown message in NetworkMessageInterpreter: " + msgType)
    }
  }

  def receive = {
    case InterpretMessage(message) => interpretMessage(message)
    case _ => println("Error: from interpreter.")
  }
}
