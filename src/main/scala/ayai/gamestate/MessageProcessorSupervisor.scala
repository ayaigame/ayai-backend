package ayai.gamestate

/**
 * ayai.gamestate.MessageProcessorSupervisor
 * Supervising actor for the MessageProcessor
 */

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate
import akka.routing.FromConfig

/** External Imports **/
import scala.concurrent.duration._
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.collection.mutable.HashMap

object NetworkMessageProcessorSupervisor {
  def apply(world: RoomWorld, socketMap: ConcurrentMap[String, String]) = new NetworkMessageProcessorSupervisor(world, socketMap)
}

class NetworkMessageProcessorSupervisor(world: RoomWorld, socketMap: ConcurrentMap[String, String]) extends Actor {

  // Escalate exceptions, try up to 10 times, if one actor fails, try just that one again
  val escalator = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 5 seconds) {
    case _: Exception => Escalate
  }

val router = context.system.actorOf(Props(
  NetworkMessageProcessor(world, socketMap)).withRouter(FromConfig.withSupervisorStrategy(escalator)), name = "processorrouter")

  def receive = {
    case message: ProcessMessage =>
      router forward message 
    case _ =>
      println("Error: in interpreter supervisor")
  }

}
