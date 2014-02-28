package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

class NetworkMessageQueue extends Actor{
  var messages : ArrayBuffer[NetworkMessage] = new ArrayBuffer[NetworkMessage]()

  def receive = {
    case FlushMessages() =>
      var currentMessages: Array[NetworkMessage] = messages.toArray
      messages = new ArrayBuffer[NetworkMessage]()
      sender ! new QueuedMessages(currentMessages)
    case AddInterpretedMessage(message) =>
      messages += message
    case _ => println("Error: from queue.")
  }
}
