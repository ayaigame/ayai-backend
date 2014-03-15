package ayai.systems

import crane.TimedSystem

import ayai.components._

object DirectorSystem {
  def apply() = new DirectorSystem()
}

class DirectorSystem extends TimedSystem(2000) {
  override def processTime(delta: Int) {
    val entities = world.getEntitiesByComponents(classOf[Character], classOf[Faction])
    val factions = entities.groupBy(e => (e.getComponent(classOf[Faction])) match {
      case(Some(f: Faction)) => f.name
      case _ => ""
    })

    for((faction, i) <- factions.values.view.zipWithIndex) {
      val otherIndex = i match {
        case x if x + 1 >= factions.values.size => 0
        case _ => i + 1
      }

      val position = factions.values.toList(otherIndex)(0).getComponent(classOf[Position]: @unchecked) match {
        case(Some(p: Position)) => p
      }

      faction.foreach{ entity => 
        (entity.getComponent(classOf[Goal]): @unchecked) match {
          case(Some(g: Goal)) => g.goal = new MoveTo(position)
          case _ => ()
        }
      }
    }
  }
}
