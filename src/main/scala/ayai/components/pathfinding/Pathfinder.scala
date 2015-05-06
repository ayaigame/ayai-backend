package ayai.components.pathfinding

import ayai.components.Position
import ayai.factories.{NodeState, Node}
import ayai.factories.NodeState.NodeState
import crane.Component

import scala.collection.mutable

class NodeMatrixGraphView(nodeMatrix: Array[Array[Node]], movementStyle: GridMovementStyle) {
  def apply(pos: Position): NodeState = nodeMatrix(pos.x)(pos.y).state

  // Make this generic? Not yet, anyway.
  private val terrainCosts = Map(NodeState.NORMAL -> 1, NodeState.SLOW -> 2)
  def costOfNeighborMove(from: Position, to: Position): Double = terrainCosts(apply(to))

  def neighbors(pos: Position): List[Position] = {
    // I should change the calculator to take a map, and return only adjacent positions that are not blocked.
    // That will allow forbidding diagonal movement through walls.
    // Depending on algorithm details, it might cause redundant lookups in map data. Try to minimize that.

    val adjacentPositions = movementStyle.neighboringPositionFinder.neighboringPositions(pos)
    adjacentPositions.filterNot(pos => apply(pos) == NodeState.IMPASS)
  }
}


abstract class Pathfinder(movementStyle: GridMovementStyle) extends Component {
  type NodeMatrix = Array[Array[Node]]

  def findPath(map: NodeMatrix, start: Position, goal: Position): Option[Seq[Position]] = {
    findPath(new NodeMatrixGraphView(map, movementStyle), start, goal)
  }

  def findPath(graph: NodeMatrixGraphView, start: Position, goal: Position): Option[Seq[Position]]

  // should I add findPathAsMoves, that returns "down, right, etc."?
}

/**
 * Pathfinding component which implements the A* pathfinding algorithm
 *
 * http://en.wikipedia.org/wiki/A*_search_algorithm
 */
class AStarPathfinder(movementStyle: GridMovementStyle) extends Pathfinder(movementStyle) {
  def findPath(graph: NodeMatrixGraphView, start: Position, goal: Position): Option[Seq[Position]] = {
    // this method is ported from these example implementations:
    // http://www.redblobgames.com//pathfinding/a-star/implementation.html#sec-1-4
    // http://www.redblobgames.com//pathfinding/a-star/implementation.html#sec-2-4

    val frontier = new mutable.PriorityQueue[PrioritizedValue[Double, Position]]
    frontier.enqueue(PrioritizedValue[Double, Position](0, start))
    val cameFrom = new mutable.HashMap[Position, Position]
    val costSoFar = new mutable.HashMap[Position, Double]

    while (! frontier.isEmpty) {
      val current: Position = frontier.dequeue().value

      if (current == goal) {
        return Some( reconstructPath(start, goal, cameFrom) )
      }

      for (next <- graph.neighbors(current)) {
        val newCost = costSoFar(current) + graph.costOfNeighborMove(current, next)

        if (! costSoFar.contains(next) || newCost < costSoFar(next)) {
          costSoFar += (next -> newCost)
          val priority = newCost + movementStyle.distanceHeuristic.estimateDistance(next, goal)
          frontier.enqueue(PrioritizedValue(priority, next))
          cameFrom += (next -> current)
        }
      }
    }

    // the whole graph was explored, but we never reached the goal
    return None
  }

  /**
   * An generic object to put in a PriorityQueue, for when you want to
   * set explicit priorities instead of relying on the values' natural ordering.
   * @tparam P The type of the priority, e.g. Int or Double. Must be Ordered.
   * @tparam V The type of the value, which is the data you actually care about.
   */
  private case class PrioritizedValue[P <% Ordered[P], V](val priority: P, val value: V) extends Ordered[PrioritizedValue[P, V]] {
    override def compare(that: PrioritizedValue[P, V]): Int = priority.compare(that.priority)
  }

  private def reconstructPath(start: Position, goal: Position, cameFrom: collection.Map[Position, Position]): Seq[Position] = {
    // this method is ported from these example implementations:
    // http://www.redblobgames.com//pathfinding/a-star/implementation.html#sec-1-3
    // http://www.redblobgames.com//pathfinding/a-star/implementation.html#sec-2-3
    
    val path = new mutable.ArrayBuffer[Position]
    var current = goal
    path.append(current)
    while (current != start) {
      current = cameFrom(current)
      path.append(current)
    }
    path
  }
}
