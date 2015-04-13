package ayai.components.pathfinding

import ayai.components.Position

trait DistanceHeuristic {
  def estimateDistance(start: Position, end: Position): Long
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

  def estimateDistance(start: Position, end: Position): Long = {
    val dx = math.abs(start.x - end.x)
    val dy = math.abs(start.y - end.y)

    SCALE_FACTOR * (dx + dy)
  }
}

/**
 * Heuristic class for calculating the Diagonal distance between two points
 *
 * d(p, q) = for all coordinates i: max(|pi - qi|)
 *
 * http://en.wikipedia.org/wiki/Chebyshev_distance
 */
class DiagonalDistance extends DistanceHeuristic {
  private val SCALE_FACTOR = 1

  def estimateDistance(start: Position, end: Position): Long = {
    val dx = math.abs(start.x - end.x)
    val dy = math.abs(start.y - end.y)

    SCALE_FACTOR * math.max(dx, dy)
  }
}

/**
 * Heuristic class for calculating the Euclidean distance between two points
 *
 * d(p, q) = d(q, p) = sqrt((q1 - p1)^2 + … + (qn - pn)^2)
 * NOTE: This implementation doesn't take the square root to save unnecessary cycles
 * As a heuristic, we only need to know distances relative to other distances calculated by
 * the same heuristic – we don't care what the distance actually is
 *
 * http://en.wikipedia.org/wiki/Euclidean_distance
 */
class EuclideanDistance extends DistanceHeuristic {
  private val SCALE_FACTOR = 1

  def estimateDistance(start: Position, end: Position): Long = {
    val dx = math.abs(start.x - end.x)
    val dy = math.abs(start.y - end.y)

    SCALE_FACTOR * (dx*dx + dy*dy)
  }
}
