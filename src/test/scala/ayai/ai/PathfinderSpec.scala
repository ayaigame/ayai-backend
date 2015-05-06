package ayai.ai

import ayai.components.Position
import ayai.components.pathfinding._
import ayai.factories.{Node, NodeState}
import org.scalatest._

class PathfinderSpec extends FlatSpec with Matchers {
  val movementStyle = new ManhattanMovementStyle
  val pathfinder = new AStarPathfinder(movementStyle)

  "Pathfinding" should "return None when there is no valid path" in {
    val mapWithNoPath = Array(
      Array(Node(NodeState.NORMAL), Node(NodeState.IMPASS)),
      Array(Node(NodeState.IMPASS), Node(NodeState.NORMAL))
    )
    pathfinder.findPath(mapWithNoPath, Position(0, 0), Position(1, 1)) should equal(None)
  }

  it should "penalize slow terrain appropriately" in {
    // include a wall of slow that must be crossed, to check that crossing is possible
    // and a wall with a gap, to check that the gap is found
    val map = createNodeMatrixFromString(
      """. ~ . . ~ ~
        |~ ~ . ~ ~ ~
        |. . . ~ ~ ~
        |. ~ . ~ ~ ~
        |~ ~ . . . .""".stripMargin)

    // would it be more useful for the pathfinder to return a list of moves like "Right, Down" instead of a list of Positions?

    val start = Position(0, 0)
    val goal = Position(5, 4)
    val path = pathfinder.findPath(map, start, goal) match {
      case Some(path) => path
      case None => fail("path should exist")
    }

    // I don’t test exact equality because I don’t care about tiebreaker diagonal moves,
    // e.g. the first move could be (0, 1) or (1, 0)
    path.length should equal(9)
    assertPathIsValid(path, movementStyle, start, goal)
    assert(path.containsSlice(List(
      Position(2, 2),
      Position(2, 3),
      Position(2, 4),
      Position(3, 4)
    )), "path did not avoid the slow tiles")
  }

  def assertPathIsValid(path: Seq[Position], movementStyle: GridMovementStyle, start: Position, goal: Position): Unit = {
    def positionsAreAdjacent(pos1: Position, pos2: Position, movementStyle: GridMovementStyle): Boolean = {
      movementStyle.distanceHeuristic.estimateDistance(pos1, pos2) == 1
    }

    assert(positionsAreAdjacent(path.head, start, movementStyle))
    assert(path.last == goal)

    path.sliding(2).foreach({
      case Seq(pos, nextPos) =>
        assert(
          positionsAreAdjacent(pos, nextPos, movementStyle),
          s"positions $pos and $nextPos in the path are not adjacent"
        )
    })
  }

  def createNodeMatrixFromString(charGrid: String): Array[Array[Node]] = {
    val lines = charGrid.split("\\r?\\n")
    lines.map (line =>
      line.flatMap (char => char match {
          case '.' => Some( Node(NodeState.NORMAL) )
          case '~' => Some( Node(NodeState.SLOW) )
          case '#' => Some( Node(NodeState.IMPASS) )
          case _ => None
        }
      ).toArray
    ).toArray
  }
  "createNodeMatrixFromString (temporary test)" should "generate maps correctly" in {
    val mapString =
      """. ~ .
        |# # .""".stripMargin
    val map = createNodeMatrixFromString(mapString)
    val desiredMap = Array(
      Array(Node(NodeState.NORMAL), Node(NodeState.SLOW), Node(NodeState.NORMAL)),
      Array(Node(NodeState.IMPASS), Node(NodeState.IMPASS), Node(NodeState.NORMAL))
    )
    map should equal(desiredMap)
  }
}
