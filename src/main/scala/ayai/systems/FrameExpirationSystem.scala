package ayai.systems

import akka.actor.{Actor, ActorRef, ActorSystem}

import crane.EntityProcessingSystem
import crane.Entity

import ayai.components._
import ayai.networking.ConnectionWrite

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._

object FrameExpirationSystem {
  def apply(actorSystem: ActorSystem) = new FrameExpirationSystem(actorSystem: ActorSystem)
}
/**
** For an entity with a Frame, check if its ready and then kill it
** Else add one to its frameCount
** (Probably should not be killing them, must do some action)
**/
class FrameExpirationSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Frame])) {
  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent(classOf[Frame]) match {
      case Some(frame: Frame) => {
        if(frame.isReady) {
          e.getComponent(classOf[Projectile]) match {
            case Some(projectile: Projectile) => {
              val json = ("type" -> "disconnect") ~
                          ("id" -> projectile.id)
              val actorSelectionDisc = actorSystem.actorSelection("user/SockoSender*")
              actorSelectionDisc ! new ConnectionWrite(compact(render(json)))
            }

            case _ =>
          }

          e.kill()
        }

        frame.framesActive -= 1
      }
      case _ =>
    }
  }
}
