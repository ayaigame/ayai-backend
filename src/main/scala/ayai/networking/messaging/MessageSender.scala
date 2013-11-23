package ayai.networking.messaging
/**
 * ayai.networking.messaging.MessagingSender
 * Actor that sends messages to a player over a websocket
 */

/** Ayai Imports **/
import ayai.persistence.User

/** Akka Imports **/
import akka.actor.Actor

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

class MessageSender extends Actor {
  var lastMessage: String = ""
  var ws = None : Option[WebSocketFrameEvent]

  def receive = {
    case event : WebSocketFrameEvent =>
      ws = Some(event)
      println("Associated")

    case MessageHolder(message) =>
      lastMessage = message.message
      sendUser(message)

    case CheckIn(receiver) =>
      unreadMessages(receiver)

    case _ => println("Unknown message type")
   }

  def sendUser(message: Message) ={
    println("From: " + message.sender.username + "\nMessage received: " + message.message)
      
    ws match {
      case None =>
        println("WS not associated (anymore?)")
        println("Shutting down this actor")
//        context.stop(self)
      case Some(writeTo) =>
        writeTo.writeText(message.sender.username + " said: " + message.message)
    }
  }

  def unreadMessages(receiver: User) = {

  }
}
