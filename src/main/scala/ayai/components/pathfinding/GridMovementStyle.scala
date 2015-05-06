package ayai.components.pathfinding

trait GridMovementStyle {
  def distanceHeuristic: DistanceHeuristic
  def neighboringPositionFinder: NeighboringPositionFinder
}

/**
 * Movement by one square up, down, right, or left.
 */
class ManhattanMovementStyle extends GridMovementStyle {
  override def distanceHeuristic: DistanceHeuristic = new ManhattanDistance
  override def neighboringPositionFinder: NeighboringPositionFinder = new ManhattanNeighboringPositionFinder
}

/**
 * Movement along axes or diagonally, to eight possible squares, like a king on a chessboard.
 */
class ChebyshevMovementStyle extends GridMovementStyle {
  override def distanceHeuristic: DistanceHeuristic = new ChebyshevDistance
  override def neighboringPositionFinder: NeighboringPositionFinder = new ChebyshevNeighboringPositionFinder
}

/**
 * Movement along axes or diagonally, to eight possible squares, like a king on a chessboard.
 * But disallowing diagonal movement that would clip the corner of a wall.
 */
class WallSensitiveChebyshevMovementStyle extends GridMovementStyle {
  override def distanceHeuristic: DistanceHeuristic = new ChebyshevDistance
  override def neighboringPositionFinder: NeighboringPositionFinder = new WallSensitiveChebyshevNeighboringPositionFinder
}
