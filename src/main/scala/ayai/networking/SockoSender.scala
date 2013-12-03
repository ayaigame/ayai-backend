package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame

class SockoSender(ws: WebSocketFrameEvent) extends Actor {
  var webSocket = ws
  def receive = {
    case ConnectionWrite(message) => {
      if(ws.isInstanceOf[CloseWebSocketFrame]) {
        context.stop(self)
      } else {
        ws.writeText(message)
      }
    }    
    case newFrame: WebSocketFrameEvent => webSocket = newFrame
    case _ => println("Error from SockoSender")
  }
}
