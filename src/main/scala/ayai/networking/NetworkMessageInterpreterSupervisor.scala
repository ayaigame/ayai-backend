package ayai.networking

/**
 * ayai.networking.NetworkMessageInterpreterSupervisor
 * Supervising actor for the NetworkMessageInterpreter
 */

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate
import akka.routing.FromConfig

/** External Imports **/
import scala.concurrent.duration._

class NetworkMessageInterpreterSupervisor(queue: ActorRef) extends Actor {

  // Escalate exceptions, try up to 10 times, if one actor fails, try just that one again
  val escalator = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 5 seconds) {
    case _: Exception => Escalate
  }

  val router = context.system.actorOf(Props(
    new NetworkMessageInterpreter(queue)).withRouter(
      FromConfig.withSupervisorStrategy(escalator)), 
    name="interpreterrouter")

  def receive = {
    case message: InterpretMessage =>
      router forward message 
    case _ =>
      println("Error: in interpreter supervisor")
  }

}
