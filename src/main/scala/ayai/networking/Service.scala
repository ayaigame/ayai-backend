package ayai.networking

/** Akka Imports **/
import akka.actor.{Actor, ActorRef}

abstract class Service(connection: ActorRef) extends Actor {
  def acceptMessages
}
