package ayai.systems.ai

import crane.{EntityProcessingSystem, Entity, World}

import ayai.components._

object DirectorSystem {
  def apply() = new DirectorSystem()
}

class DirectorSystem extends System {
  override def process(deltaTime: Int) {
    val entitites = world.getEntitiesByComponents(classOf[Character], classOf[Faction])
    val factions = entities.groupBy((_.getComponent(classOf[Faction])) match {
      case(Some(h: Faction)) => f.name
    })

    for((faction, i) <- factions.view.zipWithIndex) {
      val otherIndex = i.match {
        case x if x + 1 >= factions.length => x
        case _ => i + 1
      }
      val position = factions(otherIndex)(0).getComponent(classOf[Position]: @unchecked) match {
          case(Some(p: Position)) => p
        }

      faction.foreach{ entity => 
        val goal = (e.getComponent(classOf[Goal]): @unchecked) match {
          case(Some(g: Character)) => g
        }
        goal.goal = new MoveTo(position)
      }
    }
  }
}
