package ayai.networking

/** Ayai Imports **/
import ayai.gamestate._

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorRef

class NetworkMessageProcessor(connectionManager: ActorRef) extends Actor {
  def processMessage(message: NetworkMessage) = {
    var pR = message match {
      case playerRequest: PlayerRequest => playerRequest
      case _ => throw new ClassCastException
    }
    val roomJSON: String = GameState.getPlayerRoomJSON(pR.getPlayerId)
    connectionManager ! new WriteToConnection(pR.getConnectionId, roomJSON)
  }

  def receive = {
    case ProcessMessage(message) => processMessage(message)
    case _ => println("Error: from interpreter.")
  }
}