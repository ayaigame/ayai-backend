package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

class PlayerRequest(connectionId: Int, playerId: Int) extends NetworkMessage(connectionId) {
  
}

