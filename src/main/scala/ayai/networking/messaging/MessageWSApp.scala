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
        val rootJSON = parse(wsFrame.readText)

        val tempUsername: String = compact(render(rootJSON \ "username"))
        val user = UserQuery.getByUsername(tempUsername.substring(1, tempUsername.length - 1))

        user match {
          case Some(fUser) =>
            val actorName = "ms" + fUser.id
            actorSystem.actorOf(Props[MessageSender], name=actorName ) ! wsFrame
          case _ =>
            println("Cant find you")
        }
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
      var sender = None: Option[User]


      val tempSender: String = compact(render(rootJSON \ "sender"))
      sender = UserQuery.getByUsername(tempSender.substring(1, tempSender.length - 1))

      sender match {
        case Some(sUser)  =>
          println("Got: " + sUser)
          var mh:MessageHolder = null

          msgType match {
            case "public" =>
              mh = new MessageHolder(new PublicMessage(message, sUser))
            case "private" =>
              //val receiver: String = compact(render(rootJSON \ "receiver"))
              mh = new MessageHolder(new PrivateMessage(message, sUser, sUser))
            case _ =>
              println(msgType)
          }
          actorSystem.actorOf(Props[MessageReceiver]) ! mh

        case _ =>
          println("Got nothing...")
      }
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
