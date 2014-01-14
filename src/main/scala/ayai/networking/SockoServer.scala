package ayai.networking

/** Socko Imports **/
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

/** Akka Imports **/
import akka.actor.ActorSystem
import akka.actor.ActorRef


/** SockoServer
 * Runs a server which will pass packets on to the Interpreter
 **/

class SockoServer(actorSystem: ActorSystem, interpreter: ActorRef, queue: ActorRef) extends Logger {
  // val actorSystem = ActorSystem("MessageReceiverWSActorSystem")
  val routes = Routes({

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/") => {
        wsHandshake.authorize(onClose = Some(testOnCloseCallback))
      }
    }

    case WebSocketFrame(wsFrame) => {
        interpreter ! new InterpretMessage(wsFrame)
    }
  })

  def testOnCloseCallback(webSocketId: String) {
    System.out.println(s"Web Socket $webSocketId closed")
    queue ! new AddInterpretedMessage(new RemoveCharacter(webSocketId))
  }

  def run(port: Int) {
    val webServer = new WebServer(WebServerConfig(port=port, hostname="0.0.0.0"), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { webServer.stop() }
    })
    webServer.start()

    System.out.println("Running SockoServer")
  }
}
