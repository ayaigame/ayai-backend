package ayai.networking

import ayai.persistence._

/** Akka Imports **/
import akka.actor.{Actor}

/** Socko Imports **/
import org.mashupbots.socko.events.{HttpRequestEvent, HttpResponseStatus}

import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._

import com.typesafe.config.ConfigFactory

class AuthorizationProcessor() extends Actor {

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

          transaction {
            token = AyaiDB.validatePassword(username, password)
          }
 
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
      val content:String = request.request.content.toString
      val delimiter = content.indexOfSlice("&")

      val username = content.slice(0, delimiter).replaceAll("email=", "")
      val password = content.slice(delimiter + 1, content.length).replaceAll("password=", "")
      transaction {
        AyaiDB.registerUser(username, password)
      }
      request.response.write(HttpResponseStatus.OK, "GOOD")

    case RecoveryPost(request: HttpRequestEvent) =>
      println("RECOVERY")

    case _ => println("Error from AuthorizationProcessor.")
  }
}
