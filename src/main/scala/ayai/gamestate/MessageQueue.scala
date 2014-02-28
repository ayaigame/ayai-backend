package ayai.gamestate

import ayai.gamestate._
import ayai.networking._

/** Akka Imports **/
import akka.actor.Actor

/** External Imports **/
import scala.collection.mutable.{ArrayBuffer, HashMap}

class MessageQueue extends Actor{
  var messages: HashMap[String, ArrayBuffer[Message]] = new HashMap[String, ArrayBuffer[Message]]()

  /**
  * Returns all the messages in the queue to the sender and clears out the queue.
  **/
  def flushMessages(world: String) = {
    var currentMessages: Array[Message] = messages(world).toArray
    messages(world) = new ArrayBuffer[Message]()
    sender ! new QueuedMessages(currentMessages)
  }

  def addInterpretedMessage(world: String, message: Message) = {
    messages(world) += message
  }

  def receive = {
    case FlushMessages(world: String) => flushMessages(world)
    case AddInterpretedMessage(world: String, message: Message) => addInterpretedMessage(world, message)
    case _ => println("Error: from queue.")
  }
}
