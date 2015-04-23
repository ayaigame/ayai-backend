package ayai.components.pathfinding

import ayai.components.Position

trait DistanceHeuristic {
  def estimateDistance(start: Position, end: Position): Double
}

/**
 * Heuristic class for calculating the Manhattan distance between two points
 *
 * d(p, q) = sum from i to n of (|pi - qi|) where n is the dimension
 *
 * http://en.wikipedia.org/wiki/Taxicab_geometry
 */
class ManhattanDistance extends DistanceHeuristic {
  private val SCALE_FACTOR = 1

  def estimateDistance(start: Position, end: Position): Double = {
    val dx = math.abs(start.x - end.x)
    val dy = math.abs(start.y - end.y)

    SCALE_FACTOR * (dx + dy)
  }
}

/**
 * Heuristic class for calculating the Chebyshev distance between two points
 *
 * d(p, q) = for all coordinates i: max(|pi - qi|)
 *
 * http://en.wikipedia.org/wiki/Chebyshev_distance
 */
class ChebyshevDistance extends DistanceHeuristic {
  private val SCALE_FACTOR = 1

  def estimateDistance(start: Position, end: Position): Double = {
    val dx = math.abs(start.x - end.x)
    val dy = math.abs(start.y - end.y)

    SCALE_FACTOR * math.max(dx, dy)
  }
}

/**
 * Heuristic class for calculating the Euclidean distance between two points
 *
 * d(p, q) = d(q, p) = sqrt((q1 - p1)^2 + … + (qn - pn)^2)
 *
 * http://en.wikipedia.org/wiki/Euclidean_distance
 */
class EuclideanDistance extends DistanceHeuristic {
  private val SCALE_FACTOR = 1

  def estimateDistance(start: Position, end: Position): Double = {
    val dx = math.abs(start.x - end.x)
    val dy = math.abs(start.y - end.y)

    SCALE_FACTOR * math.sqrt(dx*dx + dy*dy)
  }
}
