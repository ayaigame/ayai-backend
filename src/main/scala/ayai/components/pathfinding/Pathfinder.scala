package ayai.components.pathfinding

import ayai.components.Position
import ayai.factories.Node
import crane.Component

abstract class Pathfinder(heuristic: DistanceHeuristic) extends Component {
  type Matrix2D = Array[Array[Node]]

  def findPath(map: Matrix2D, start: Position, end: Position): Option[List[Position]]
}

/**
 * Pathfinding component which implements the A* pathfinding algorithm
 *
 * http://en.wikipedia.org/wiki/A*_search_algorithm
 *
 * @param heuristic Instance of DistanceHeuristic to measure distance during A*
 */
class AStar(heuristic: DistanceHeuristic) extends Pathfinder(heuristic) {
  def findPath(map: Matrix2D, start: Position, end: Position): Option[List[Position]] = {
    println(heuristic.estimateDistance(start, end))

    // TODO: Implement A* :P

    None
  }
}
