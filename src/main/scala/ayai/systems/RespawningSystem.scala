package ayai.systems

/** Ayai Imports **/
import ayai.components._

/** Crane Imports **/
import crane.{Entity, EntityProcessingSystem, World}

object RespawningSystem {
  def apply() = new RespawningSystem()
}

class RespawningSystem() extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Respawn])) { 
  override def processEntity(e: Entity, delta: Int) {
    var respawn = (e.getComponent(classOf[Respawn]): @unchecked) match {
      case (Some(r: Respawn)) => r
    }
    val health = (e.getComponent(classOf[Health]): @unchecked) match {
      case(Some(h: Health)) => h
    }
    val room = (e.getComponent(classOf[Room]): @unchecked) match {
      case Some(r: Room) => r
    }

    val position = (e.getComponent(classOf[Position]): @unchecked) match {
      case Some(p: Position) => p 
    }
    
    if(respawn.isReady(System.currentTimeMillis())) {
        health.refill()
      e.removeComponent(classOf[Respawn])
      e.components += new Transport(position, room)
    }
  }
}
