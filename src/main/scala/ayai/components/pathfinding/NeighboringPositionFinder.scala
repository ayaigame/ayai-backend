package ayai.components.pathfinding

import ayai.components.Position

trait NeighboringPositionFinder {
  def neighboringPositions(pos: Position): List[Position]
}

/**
 * Returns the positions marked 'x' on this diagram, where 'o' is the given position:
 *
 * .x.
 * xox
 * .x.
 */
class ManhattanNeighboringPositionFinder extends NeighboringPositionFinder {
  override def neighboringPositions(pos: Position): List[Position] = {
    val coordinateDifferences = List(
      Tuple2(1, 0),
      Tuple2(-1, 0),
      Tuple2(0, 1),
      Tuple2(0, -1)
    )
    coordinateDifferences.map(differences => differences match {
      case Tuple2(xDiff, yDiff) => Position(pos.x + xDiff, pos.y + yDiff)
    }).filterNot(position => ??? /* map(position) == NodeState.IMPASS */)
    // Should this take a map or a graph?
    // I do want to be able to look up the position just by a Position. That means it should define apply().
    // But I can't use the NodeMatrixGraphView view for that, because that needs to call this to find neighbors.
    // So the object with an apply() definition will need to be an intermediate value
    // between an Array[Array[Node]] and a NodeMatrixGraphView
    // I think I will make a NodeMatrix class, and make GraphFactory return it.
    // There is one thing to check before I do: is it okay that we use `Position`s to index the grid?
    // GraphFactory seems to avoid it, but I’m not sure whether that is because
    // they thought it would be confusing to convert scaled positions to unscaled positions,
    // or because a Position is only supposed to represent something specific to the game map, and should not be generic.
    // Look at the places Position is used in the code to see if it is ever used differently,
    // or if there are any comments saying not to.
  }
}

/**
 * Returns the positions marked 'x' on this diagram, where 'o' is the given position:
 *
 * xxx
 * xox
 * xxx
 */
class ChebyshevNeighboringPositionFinder extends NeighboringPositionFinder {
  override def neighboringPositions(pos: Position): List[Position] = {
    val coordinateDifferences = List(
      Tuple2(1, 0),
      Tuple2(1, 1),
      Tuple2(0, 1),
      Tuple2(-1, 1),
      Tuple2(-1, 0),
      Tuple2(-1, -1),
      Tuple2(0, -1),
      Tuple2(1, -1)
    )
    coordinateDifferences.map(differences => differences match {
      case Tuple2(xDiff, yDiff) => Position(pos.x + xDiff, pos.y + yDiff)
    })
  }
}

/**
 * Returns the positions marked 'x' on this diagram, where 'o' is the given position:
 *
 * xxx
 * xox
 * xxx
 *
 * But also avoids crossing diagonals when that would mean passing through the corner of a wall '#':
 *
 * xx#   xx.   .#.
 * xox   xo#   xo#
 * xxx   xx.   xx.
 */
class WallSensitiveChebyshevNeighboringPositionFinder extends NeighboringPositionFinder {
  override def neighboringPositions(pos: Position): List[Position] = {
    // I need the map to be passed to implement this.
    // So this object will probably be merged with the NodeMatrixGraphView in some way.
    ???
  }
}
