package ayai.networking

import ayai.persistence._
import ayai.factories.ClassFactory

/** Akka Imports **/
import akka.actor.{Actor}

/** Socko Imports **/
import org.mashupbots.socko.events.{HttpRequestEvent, HttpResponseStatus}

/** External Imports **/
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class AuthorizationProcessor extends Actor {

  Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
      Session.create(
      java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
      new H2Adapter))

  def receive = {
  case LoginPost(request: HttpRequestEvent) =>
    val headers = request.request.headers
    headers.get("Authorization") match {
    case Some(basicAuth: String) =>
      val auth = new String(new sun.misc.BASE64Decoder().decodeBuffer(basicAuth.replaceAll("Basic ","")))
      val delimiter = auth.indexOfSlice(":")
      val username = auth.slice(0, delimiter)
      val password = auth.slice(delimiter + 1, auth.length)

      // For signing cookies later
      val conf = ConfigFactory.load
      val serverConf = conf.getConfig("server")
      lazy val secret: String = serverConf.getString("secret")

      var token: String = ""

      token = AyaiDB.validatePassword(username, password)

      token match {
      case "" =>
        request.response.write(HttpResponseStatus.UNAUTHORIZED)
      case _ =>
        request.response.write(HttpResponseStatus.OK, token)
      }
    case _ =>
      request.response.write(HttpResponseStatus.UNAUTHORIZED)

    }

  case RegisterPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")

    val username = content.slice(0, delimiter).replaceAll("email=", "")
    val password = content.slice(delimiter + 1, content.length).replaceAll("password=", "")
    if(AyaiDB.registerUser(username, password)) {
      var token: String = ""
      token = AyaiDB.validatePassword(username, password)

      token match {
        case "" =>
          request.response.write(HttpResponseStatus.UNAUTHORIZED)
        case _ =>
          request.response.write(HttpResponseStatus.OK, token)
      }
    }
    else
      request.response.write(HttpResponseStatus.CONFLICT)

  case CharactersPost(request: HttpRequestEvent) =>
    val content:String = request.request.content.toString
    var accountId: Long = -1

    transaction {
      accountId = AyaiDB.tokens.where(token => token.token === content).single.account_id
    }
    CharacterTable.characterList(request, accountId)

  case ClassListGet(request: HttpRequestEvent) =>
    request.response.write(HttpResponseStatus.OK, compact(render(ClassFactory.asJson)))

  case CreateCharacterPost(request: HttpRequestEvent) =>
    val content:String = request.request.content.toString
    var accountId: Long = -1
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")

    val userToken = content.slice(0, delimiter).replaceAll("token=", "")
    val characterName = content.slice(delimiter + 1, delimiter2).replaceAll("name=", "")
    val className = content.slice(delimiter2 + 1, content.length).replaceAll("class=", "")
    transaction {
      accountId = AyaiDB.tokens.where(token => token.token === userToken).single.account_id
    }
    CharacterTable.createCharacter(characterName, className, accountId)
    request.response.write(HttpResponseStatus.OK, "GOOD")

  case RecoveryPost(request: HttpRequestEvent) =>
    println("RECOVERY")

  case _ => println("Error from AuthorizationProcessor.")
  }
}
