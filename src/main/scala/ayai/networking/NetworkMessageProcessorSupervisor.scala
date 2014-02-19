package ayai.networking

/**
 * ayai.networking.NetworkMessageProcessorSupervisor
 * Supervising actor for the NetworkMessageProcessor
 */

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, Props, OneForOneStrategy}
import akka.routing.RoundRobinRouter
import akka.actor.SupervisorStrategy.Escalate

/** Crane Imports **/
import crane.{World}

/** External Imports **/
import scala.concurrent.duration._
import scala.collection.concurrent.{Map => ConcurrentMap}

class NetworkMessageProcessorSupervisor(world: World, socketMap: ConcurrentMap[String, String]) extends Actor {

  // Escalate exceptions, try up to 10 times, if one actor fails, try just that one again
  val escalator = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 5 seconds) {
    case _: Exception => Escalate
  }

  val router = context.system.actorOf(Props(new NetworkMessageProcessor(context.system, world, socketMap)).withRouter(
    RoundRobinRouter(5, supervisorStrategy = escalator)))

  def receive = {
    case message: ProcessMessage =>
      router forward message 
    case _ =>
      println("Error: in interpreter supervisor")
  }

}
