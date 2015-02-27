package ayai.networking

/** Ayai Imports **/

import akka.util.Timeout
import ayai.gamestate._

/** Socko Imports **/
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.events.HttpRequestEvent
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig

/** Akka Imports **/
import akka.actor.ActorSystem
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

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
  val duration = 2 seconds
  implicit val timeout = Timeout(duration)

  val routes = Routes({
    case HttpRequest(httpRequest) => httpRequest match {
      case POST(Path("/login")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new LoginPost(httpRequest)
      }

      case POST(Path("/register")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new RegisterPost(httpRequest)
      }

      case POST(Path("/recover")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new RecoveryPost(httpRequest)
      }

      case POST(Path("/npc")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new NPCPost(httpRequest)
      }

      case POST(Path("/quest")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new QuestPost(httpRequest)
      }

      case POST(Path("/class")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new ClassPost(httpRequest)
      }

      case POST(Path("/item")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new ItemPost(httpRequest)
      }

      case POST(Path("/effect")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new EffectPost(httpRequest)
      }

      case POST(Path("/spell")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new SpellPost(httpRequest)
      }

      case POST(Path("/chars")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new CharactersPost(httpRequest)
      }

      case POST(Path("/create")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new CreateCharacterPost(httpRequest)
      }

      // case GET(Path("/classes")) => {
      //   if (httpRequest.request.is100ContinueExpected)
      //     httpRequest.response.write100Continue

      //   authorization ! new ClassListGet(httpRequest)
      // }

      case GET(Path("/npcs")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new NPCGet(httpRequest)
      }

      case GET(Path("/effects")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new EffectGet(httpRequest)
      }

      case GET(Path("/items")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new ItemGet(httpRequest)
      }

      case GET(Path("/classes")) => {
        answerWith100IfNecessary(httpRequest)
        authorization ! new ClassGet(httpRequest)
      }

      case _ => {
        answerWith100IfNecessary(httpRequest)

        println ("Error from Router")
      }
    }

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/") => wsHandshake.authorize(onClose = Some(testOnCloseCallback))
    }

    case WebSocketFrame(wsFrame) => interpreter ! new InterpretMessage(wsFrame)
  })

  def testOnCloseCallback(socketId: String) {
    println(s"Web Socket $socketId closed")
    val future = (actorSystem.actorSelection("user/SocketUserMap") ? GetUserId(socketId)).flatMap(value => {
      val userId = value.asInstanceOf[String]
      actorSystem.actorSelection("user/UserRoomMap") ? GetWorld(userId)
    }).map(_.asInstanceOf[RoomWorld])
    val worldId = Await.result(future, duration).id
    println(worldId)
    queue ! new AddInterpretedMessage(worldId, new RemoveCharacter(socketId))
  }

  def run(port: Int) {
    val webServer = new WebServer(WebServerConfig(port=port, hostname = "0.0.0.0"), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run() {
        webServer.stop()
      }
    })

    webServer.start()

    println("Running SockoServer")
  }

  private def answerWith100IfNecessary(requestEvent: HttpRequestEvent): Unit = {
    if (requestEvent.request.is100ContinueExpected) {
      requestEvent.response.write100Continue()
    }
  }
}
