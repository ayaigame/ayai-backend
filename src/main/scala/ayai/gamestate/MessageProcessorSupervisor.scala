package ayai.gamestate

/**
 * ayai.gamestate.MessageProcessorSupervisor
 * Supervising actor for the MessageProcessor
 */

import ayai.networking._

/** Akka Imports **/
import akka.actor.{Actor, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate
import akka.routing.RoundRobinRouter

/** External Imports **/
import scala.concurrent.duration._

object MessageProcessorSupervisor {
  def apply(world: RoomWorld) = new MessageProcessorSupervisor(world)
}

class MessageProcessorSupervisor(world: RoomWorld) extends Actor {

  // Escalate exceptions, try up to 10 times, if one actor fails, try just that one again
  val escalator = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 5 seconds) {
    case _: Exception => Escalate
  }

  val id = world.id
  val router = context.system.actorOf(Props(
                MessageProcessor(world)).withRouter(RoundRobinRouter(nrOfInstances = 3)),
                name = s"processorrouter$id")

  def receive = {
    case message: ProcessMessage => router forward message
    case _ => println("Error: in procesor supervisor")
  }

}
