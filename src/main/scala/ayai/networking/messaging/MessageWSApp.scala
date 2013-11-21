package ayai.networking.messaging

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
import akka.actor.Props

/** External Imports **/
import net.liftweb.json._

/** MessageSenderWSApp
 * Handles sending messages back over websocket
 * Is created by the MessageReceiverWSApp
 **/
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

/** Message ReceiverWSApp
 * Handles message sent to the system over websocket
 * Sends the message to be stored/sent to the correct user
 * Creates and shares it's actor system with MessageSenderWSApp
 **/

object MessageReceiverWSApp extends Logger {
  val actorSystem = ActorSystem("MessageReceiverWSActorSystem")
  val routes = Routes({
    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/websocket/") => {
        wsHandshake.authorize()
      }
    }
    case WebSocketFrame(wsFrame) => {
      val rootJSON = parse(wsFrame.readText)

      val tempType:String = compact(render(rootJSON \ "type"))
      val msgType:String = tempType.substring(1, tempType.length - 1)

      val message = compact(render(rootJSON \ "message"))
      val user = new User(1, "tim", "tim")

      UserQuery.getByID(1) match {
        case Some(tim)  =>
          println("Got: " + tim)
        case _ =>
          println("Got nothing...")
      }

      var mh:MessageHolder = null

      msgType match {
        case "public" =>
          mh = new MessageHolder(new PublicMessage(message, user))
        case "private" =>
          //val receiver: String = compact(render(rootJSON \ "receiver"))
          mh = new MessageHolder(new PrivateMessage(message, user, user))
        case _ =>
          println(msgType)
      }
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
