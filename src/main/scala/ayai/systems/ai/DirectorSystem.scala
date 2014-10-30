package ayai.systems

import crane.{Entity, TimedSystem}

import java.rmi.server.UID

/** Akka Imports **/
import akka.actor.{ActorSystem, Props}
import ayai.components._
import ayai.factories.EntityFactory

object DirectorSystem {
  def apply(actorSystem: ActorSystem) = new DirectorSystem(actorSystem)
}

class DirectorSystem(actorSystem: ActorSystem) extends TimedSystem(3000) {
  override def processTime(delta: Int) {
    val entities = world.getEntitiesByComponents(classOf[Character], classOf[Faction])
    val factions = entities.groupBy(e => e.getComponent[Faction]).filterKeys(_.isDefined).map {
      case (key, value) => (key.get.name, value)
    }

    // Create enough entities to make it even in healthwise-ness

    val factionHealths = factions.map {
      case (key, faction) => (key, faction.foldLeft(0) {
        (x: Int, y: Entity) =>  
          y.getComponent[Health] match {
            case Some(yh) => x + yh.maximumHealth
            case _ => x
          }
      })
    }


    val factionToAdd = factionHealths.map{
      case (name, factionHealth) =>
        if(factionHealth > 0) {
          (name, (factionHealths.values.max / factionHealth) - 1)
        }
        else {
          (name, 0)
        }
    }

    factionToAdd.foreach{
      case (name, amount) => 
        for(_ <- 1 to amount) {
          val entity = EntityFactory.createAI(world, name, actorSystem)
          world.addEntity(entity)
        }
    }
    

    for((faction, i) <- factions.values.view.zipWithIndex) {
      val otherIndex = i match {
        case x if x + 1 >= factions.values.size => 0
        case _ => i + 1
      }

      val position = (factions.values.toList(otherIndex)(0).getComponent[Position]: @unchecked) match {
        case(Some(p: Position)) => p
      }

      faction.foreach{ entity => 
        (entity.getComponent[Goal]: @unchecked) match {
          case(Some(g: Goal)) => g.goal = new AttackTo(position)
          case _ => ()
        }
      }
    }
  }
}
