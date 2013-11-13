package ayai.networking

/** Ayai Imports **/
// import ayai.networking.Connection

/** Akka Imports **/
import akka.actor.Actor

class NetworkMessageQueue extends Actor{
  var messages : Array[NetworkMessage] = new Array[NetworkMessage](0)

  def flushMessages = {
    println("Flush messages")
  }

  def addMessage(message: String) = {
    println(message) 
  }

  def receive = {
    case FlushMessages() => flushMessages
    case AddMessage(message: String) => addMessage(message)
    case _ => println("Error: incomprehensible message.")
  }
}
