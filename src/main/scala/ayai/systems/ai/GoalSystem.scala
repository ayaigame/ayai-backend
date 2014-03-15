package ayai.systems

import ayai.components._
import ayai.actions._

import crane.{Entity, System}

import scala.math.abs

object GoalSystem {
  def apply() = new GoalSystem()
}

class GoalSystem extends System {
  def findDirection(entity: Entity, tp: Position): MoveDirection = {
    (entity.getComponent(classOf[Position]): @unchecked) match {
      case Some(ep: Position) =>
        val xDistance = ep.x - tp.x
        val yDistance = ep.y - tp.y

        (abs(xDistance) > abs(yDistance), xDistance > 0, yDistance > 0) match {
          case(true, true, _) =>
            LeftDirection
          case(true, false, _) =>
            RightDirection
          case(false, _ ,true) =>
            UpDirection
          case(false, _, false) =>
            DownDirection
        }
      case _ =>
        new MoveDirection(0,0)
    
    }
  }

  override def process(delta: Int) {
    val entities = world.getEntitiesByComponents(classOf[Goal], classOf[Position], classOf[Actionable])
    for(entity <- entities) {
      val goal = (entity.getComponent(classOf[Goal]): @unchecked) match {
        case Some(g: Goal) => g
      }
      val direction = findDirection(entity, goal.goal.asInstanceOf[MoveTo].position)
      (entity.getComponent(classOf[Actionable]): @unchecked) match {
        case Some(actionable: Actionable) =>
          actionable.active = true
          actionable.action = direction
      }
      // TODO: instead of findDirection, use A*
  
    }
  }
}
