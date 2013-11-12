package ayai.main.networking

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

abstract class Service(connection: Connection) extends Actor {
  def serve
}
