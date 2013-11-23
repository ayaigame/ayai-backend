package ayai.networking

/** Ayai Imports **/
import ayai.gamestate._

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.ActorRef

class NetworkMessageProcessor(actorSystem: ActorSystem) extends Actor {
  def processMessage(message: NetworkMessage) = {
    val actorSelection = context.system.actorSelection("user/SockoSender*")
    actorSelection ! new ConnectionWrite("HI")
  }

  def receive = {
    case ProcessMessage(message) => processMessage(message)
    case _ => println("Error: from interpreter.")
  }
}