package ayai.systems

/** Ayai Imports **/
import ayai.components.{AI, Position, Character, Actionable}
import ayai.actions._

/** Crane Imports **/
import crane.{Entity, System}

/** External Imports **/
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.math.abs

class AISystem() extends System {

  def getScore(current: Position, goal : Position): Int = {
    val dx = abs(current.x - goal.x)
    val dy = abs(current.y - goal.y)
    val dist = dx + dy
    return dist
  }

  def findClosest(entity: Entity, possibles: List[Entity]): Entity = {
    val scores = HashMap[Entity, Int]()

    for(p <- possibles) {
      (entity.getComponent(classOf[Position]), p.getComponent(classOf[Position])) match {
        case(Some(ep: Position), Some(pp: Position)) =>
          scores(p) = getScore(ep, pp)
        case _ =>
          println("BAD")
      }
    }

    return (scores.toList sortBy {_._2}).head._1
  }

  def findDirection(entity: Entity, target: Entity): MoveDirection = {
    (entity.getComponent(classOf[Position]), target.getComponent(classOf[Position])) match {
      case(Some(ep: Position), Some(tp: Position)) =>
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
    val entities = world.getEntitiesByComponents(classOf[AI], classOf[Position])
    val targets = world.getEntitiesWithExclusions(include=List(classOf[Character], classOf[Position]), exclude=List(classOf[AI]))

    for(entity <- entities) {
      // Find your target
      // TODO: Use QuadTrees to only get neighbors
      if(!targets.isEmpty) {
        val target = findClosest(entity, targets)
        val direction = findDirection(entity, target)
        entity.removeComponent(classOf[Actionable])
        entity.components += new Actionable(true, direction)
        // TODO: instead of findDirection, convert map/entities into grid, use A*
      }
    }
  }
}
