package ayai.systems

/** Ayai Imports **/
import ayai.components._
import ayai.networking._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
/** Crane Imports **/
import crane.{Entity, EntityProcessingSystem, World}

object RespawningSystem {
  def apply(actorSystem: ActorSystem) = new RespawningSystem(actorSystem)
}

class RespawningSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Respawn], classOf[NetworkingActor])) {
  override def processEntity(e: Entity, delta: Int) {
    val respawn = (e.getComponent[Respawn]: @unchecked) match {
      case (Some(r: Respawn)) => r
      case _ => null
    }
    val health = (e.getComponent[Health]: @unchecked) match {
      case (Some(h: Health)) => h
      case _ => null
    }

    val position = (e.getComponent[Position]: @unchecked) match {
      case Some(p: Position) => p
      case _ => null
    }

    if(respawn.isReady(System.currentTimeMillis())) {
      health.refill()
      e.removeComponent[Respawn]
      e.removeComponent[Dead]
    } else {
      val na = (e.getComponent[NetworkingActor]) match {
        case Some(networkActor: NetworkingActor) =>
          val json = ("type" -> "chat") ~
            ("message" -> ("Respawn in: " + respawn.timeLeft(System.currentTimeMillis()).toString)) ~
            ("sender" -> "system")
          val actorSelection = networkActor.actor
          actorSelection ! new ConnectionWrite(compact(render(json)))
        case _ =>  
      }
      
    }
  }
}