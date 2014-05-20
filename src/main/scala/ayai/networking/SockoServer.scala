package ayai.networking


/** Socko Imports **/
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.events.{HttpHeader, HttpResponseStatus}
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

/** Akka Imports **/
import akka.actor.ActorSystem

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

import ayai.gamestate._

/** SockoServer
 * Runs a server which will pass packets on to the Interpreter
 **/

object SockoServer {
  def apply(actorSystem: ActorSystem) = new SockoServer(actorSystem)
}

class SockoServer(actorSystem: ActorSystem) extends Logger {
  val authorization = actorSystem.actorSelection("user/AProcessor")
  val interpreter = actorSystem.actorSelection("user/NMInterpreter")
  val queue = actorSystem.actorSelection("user/MQueue")
  implicit val timeout = Timeout(2 seconds)

  val routes = Routes({
    case HttpRequest(httpRequest) => httpRequest match {
      case POST(Path("/login")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue
        authorization ! new LoginPost(httpRequest)
      }

      case POST(Path("/register")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new RegisterPost(httpRequest)
      }

      case POST(Path("/recover")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new RecoveryPost(httpRequest)
      }

      case POST(Path("/npc")) => {
        if(httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new NPCPost(httpRequest)
      }

      case POST(Path("/quest")) => {
        if(httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new QuestPost(httpRequest)
      }

      case POST(Path("/class")) => {
        if(httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new ClassPost(httpRequest)
      }
      case POST(Path("/item")) => {
        if(httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new ItemPost(httpRequest)
      }
      case POST(Path("/effect")) => {
        if(httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new EffectPost(httpRequest)
      }
      case POST(Path("/spell")) => {
        if(httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new SpellPost(httpRequest)
      }

      case POST(Path("/chars")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new CharactersPost(httpRequest)
      }

      case POST(Path("/create")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        authorization ! new CreateCharacterPost(httpRequest)
      }

      // case GET(Path("/classes")) => {
      //   if (httpRequest.request.is100ContinueExpected)
      //     httpRequest.response.write100Continue

      //   authorization ! new ClassListGet(httpRequest)
      // }

      case GET(Path("/npcs")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

         authorization ! new NPCGet(httpRequest)
      }
      case GET(Path("/effects")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

         authorization ! new EffectGet(httpRequest)
      }
      case GET(Path("/items")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

         authorization ! new ItemGet(httpRequest)
      }
      case GET(Path("/classes")) => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

         authorization ! new ClassGet(httpRequest)
      }
      case _ => {
        if (httpRequest.request.is100ContinueExpected)
          httpRequest.response.write100Continue

        println ("Error from Router")
      }
    }

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/") => {
        wsHandshake.authorize(onClose = Some(testOnCloseCallback))
      }
    }

    case WebSocketFrame(wsFrame) => {
        interpreter ! new InterpretMessage(wsFrame)
    }
  })

  def testOnCloseCallback(socketId: String) {
    System.out.println(s"Web Socket $socketId closed")
    val future = actorSystem.actorSelection("user/SocketUserMap") ? GetUserId(socketId)
    val userId = Await.result(future, timeout.duration).asInstanceOf[String]
    val worldFuture = actorSystem.actorSelection("user/UserRoomMap") ? GetWorld(userId)
    val worldId = Await.result(worldFuture, timeout.duration).asInstanceOf[RoomWorld].id
    println(worldId)
    queue ! new AddInterpretedMessage(worldId, new RemoveCharacter(socketId))
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
