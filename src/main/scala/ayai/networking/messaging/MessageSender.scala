package ayai.networking.messaging
/**
 * ayai.networking.messaging.MessagingSender
 * Actor that sends messages to a player over a websocket
 */

/** Ayai Imports **/
import ayai.persistence.User
import ayai.networking.Connection

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

/** External Imports **/
import scala.slick.driver.H2Driver.simple._

case class CheckIn(receiver: User)

class MessageSender extends Actor {

  def receive = {
    case MessageHolder(message) =>
      sendUser(message)
    case CheckIn(receiver) =>
      unreadMessages(receiver)
    case _ => println("Unknown message type")
   }

  def sendUser(message: Message) ={
    println("From: " + message.sender.username + "\nMessage received: " + message.message)
  }

  def unreadMessages(receiver: User) = {

  }
}
