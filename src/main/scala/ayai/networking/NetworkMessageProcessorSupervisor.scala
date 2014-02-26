package ayai.networking

/**
 * ayai.networking.NetworkMessageProcessorSupervisor
 * Supervising actor for the NetworkMessageProcessor
 */

/** Ayai Imports **/
import ayai.gamestate.RoomWorld

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate
import akka.routing.FromConfig

/** External Imports **/
import scala.concurrent.duration._
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.collection.mutable.HashMap

object NetworkMessageProcessorSupervisor {
  def apply(worlds: HashMap[String, RoomWorld], socketMap: ConcurrentMap[String, String]) = new NetworkMessageProcessorSupervisor(worlds, socketMap)
}

class NetworkMessageProcessorSupervisor(worlds: HashMap[String, RoomWorld], socketMap: ConcurrentMap[String, String]) extends Actor {

  // Escalate exceptions, try up to 10 times, if one actor fails, try just that one again
  val escalator = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 5 seconds) {
    case _: Exception => Escalate
  }

val router = context.system.actorOf(Props(
  NetworkMessageProcessor(worlds, socketMap)).withRouter(FromConfig.withSupervisorStrategy(escalator)), name = "processorrouter")

  def receive = {
    case message: ProcessMessage =>
      router forward message 
    case _ =>
      println("Error: in interpreter supervisor")
  }

}
