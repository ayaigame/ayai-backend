package ayai.networking

/** ayai.networking.messaging.MessageWSApp
 * An example socko server that runs both a sender and a receiver for WS events and passes them to actors
 * We only need one of each socko server - they handle creating as many actors as is needed
 * (typically 1 actor for each sent message, and 1 message overall for each user)
 */
 

/** Ayai Imports **/
import ayai.persistence.User
import ayai.persistence.UserQuery

/** Socko Imports **/
import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

/** Akka Imports **/
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props

/** External Imports **/
import net.liftweb.json._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Success, Failure}

/** Message ReceiverWSApp
 * Handles message sent to the system over websocket
 * Sends the message to be stored/sent to the correct user
 * Creates and shares it's actor system with MessageSenderWSApp
 **/

class SockoServer(actorSystem: ActorSystem, interpreter: ActorRef) extends Logger {
  // val actorSystem = ActorSystem("MessageReceiverWSActorSystem")
  val routes = Routes({

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/websocket/") => {
        wsHandshake.authorize()
      }
    }

    case WebSocketFrame(wsFrame) => {
        interpreter ! new InterpretMessage(wsFrame)
    }
  })

  def run(port: Int) {
    val webServer = new WebServer(WebServerConfig(port=port), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { webServer.stop() }
    })
    webServer.start()

    System.out.println("Running SockoServer")
  }
}
