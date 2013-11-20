package ayai.networking.messaging

import ayai.persistence.User

import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

import akka.actor.ActorSystem
import akka.actor.Props

object MessageSenderWSApp extends Logger {

  def run(as: ActorSystem) {
    val actorSystem = as
    val routes = Routes({
      case WebSocketHandshake(wsHandshake) => wsHandshake match {
        case Path("/websocket/") => {
          wsHandshake.authorize()
        }
      }

      case WebSocketFrame(wsFrame) => {
        actorSystem.actorOf(Props[MessageSender], name="ms1") ! wsFrame
      }
    })

    val webServer = new WebServer(WebServerConfig(port=7002), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { webServer.stop() }
    })
    webServer.start()
    System.out.println("Running MessageSenderWSApp")
  }
}

object MessageReceiverWSApp extends Logger {
  val actorSystem = ActorSystem("MessageReceiverWSActorSystem")
  val routes = Routes({
    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/websocket/") => {
        wsHandshake.authorize()
      }
    }
    case WebSocketFrame(wsFrame) => {
      val user = new User(1, "tim", "tim")
      val pumsg = new PublicMessage(wsFrame.readText, user)
      val mh = MessageHolder(pumsg)
      actorSystem.actorOf(Props[MessageReceiver]) ! mh
    }
  })

  def main(args: Array[String]) {
    val webServer = new WebServer(WebServerConfig(port=7001), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { webServer.stop() }
    })
    webServer.start()

    // Receiver kicks off Sender
    val ms = MessageSenderWSApp
    ms.run(actorSystem)

    System.out.println("Running MessagingSReceiverWSApp")
  }
}
