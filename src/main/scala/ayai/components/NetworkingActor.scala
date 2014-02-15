package ayai.components

import crane.Component

import akka.actor.{Actor, ActorSystem, ActorRef, Props, ActorSelection}

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent


class NetworkingActor(val actor: ActorSelection) extends Component {

}