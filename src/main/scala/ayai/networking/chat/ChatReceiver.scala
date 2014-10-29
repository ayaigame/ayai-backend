package ayai.networking.chat

/**
 * ayai.networking.chat.ChatReceiver
 * Actor that receives chats sent by the player over sockets and stores them
 * and forwards them to the actor that forwards chats back to players
 */

/** Ayai Imports **/
import ayai.networking.ConnectionWrite
import ayai.components.NetworkingActor
import ayai.persistence.AyaiDB

/** Akka Imports **/
import akka.actor.Actor

/** Crane Imports **/
import crane.World

/** External Imports **/
//import scala.slick.driver.H2Driver.simple.{Database,Session}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

class JChat(sender: String, text: String) {
  implicit val formats = Serialization.formats(NoTypeHints)
  implicit def asJson: JObject = {
    ("type" -> "chat") ~
    ("sender" -> sender) ~
    ("message" -> text)
  }
  override def toString: String = {
    write(this.asJson)
  }
}


class ChatReceiver extends Actor {

  def receive = {
    case ChatHolder(chat, world) =>
      val received = reroute(chat, world)
      store(chat, received)
      context.stop(self)

    case _ => println("Unknown chat type")
   }

  private def store(chat: Chat, received: Boolean) = {
    // Create the type of stored chat based on the type of chat we receive
    // This is since Public Chats do not have a reciever and are automatically
    // considered "received"
    chat match {
      case PublicChat(text, sender) =>
        AyaiDB.storePublicChat(chat.asInstanceOf[PublicChat])
      case _ =>
        println ("not yet implemented")
    }
  }  


  def reroute(chat: Chat, world: World) : Boolean = {
    chat match {
      // Attempt to send Private chat
      case PrivateChat(text, sender, receiver) =>
        // TODO: Private Chats
        //val targetFuture = context.system.actorSelection("user/SockoSender" + receiver.id).resolveOne(100.milliseconds)
        //val targetRef = Await.result(targetFuture, 100.milliseconds)
        //if(targetRef.isTerminated) {
        //  return false
        //} else {
        //  targetRef ! chatHolder
        println("Not yet implemented")
        true
        //}
      // Send Public Chat to every chat sender
      case PublicChat(text, sender) =>
        val entities = world.getEntitiesByComponents(classOf[NetworkingActor])
        entities.foreach{ e =>  
          (e.getComponent(classOf[NetworkingActor]): @unchecked) match {
            case(Some(na: NetworkingActor)) =>
              na.actor! new ConnectionWrite(new JChat(sender.name, text).toString)
          }
        }
        true
    }
  }
}
