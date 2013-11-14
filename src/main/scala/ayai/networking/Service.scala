package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

abstract class Service(connection: Connection) extends Actor {
  def acceptMessages
}
