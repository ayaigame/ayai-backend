package ayai.apps

/**
 * ayai.apps.MessagingApp
 * Runs a sample Messaging example
 */

/** Ayai Imports **/
import ayai.persistence._
import ayai.networking.messaging._

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

/** External Imports **/
import java.nio.file.{Files, Paths}
import scala.slick.driver.H2Driver.simple._

object MessagingApp {
  def main(args: Array[String]) = {
    // If DB Doesn't exist, create it
    if(!Files.exists(Paths.get("ayai.h2.db"))) {
      Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
        (Users.ddl ++ StoredMessages.ddl).create
      }
    }
  
    // Create User + Message + Actors + Fluff
    val u = new User(1, "tim", "tim")
    val pm = new PublicMessage("hello", u)
    val mh = new MessageHolder(pm)

    val system = ActorSystem("test")
    val mr =  system.actorOf(Props[MessageReceiver], name="mr")
    val ms = system.actorOf(Props[MessageSender], name="1")
    
    // Send Message
    mr ! mh
  }
}
