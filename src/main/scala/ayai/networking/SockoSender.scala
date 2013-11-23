package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

class SockoSender(ws: WebSocketFrameEvent) extends Actor {
  var webSocket = ws
  def receive = {
    case ConnectionWrite(message) => webSocket.writeText(message)
    case newFrame: WebSocketFrameEvent => webSocket = newFrame
    case _ => println("Error from SockoSender")
  }
}