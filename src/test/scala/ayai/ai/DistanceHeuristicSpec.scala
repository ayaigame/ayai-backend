package ayai.ai

/** External Imports **/

import ayai.components.Position
import ayai.components.pathfinding._
import org.scalatest._

class DistanceHeuristicSpec extends FlatSpec with Matchers {
  "Manhattan distance" should "be accurate" in {
    val heuristic = new ManhattanDistance
    heuristic.estimateDistance(Position(0, 0), Position(0, 0)) should equal(0)
    heuristic.estimateDistance(Position(0, 0), Position(0, 1)) should equal(1)
    heuristic.estimateDistance(Position(0, 0), Position(1, 0)) should equal(1)
    heuristic.estimateDistance(Position(5, 3), Position(2, 7)) should equal(7)
  }

  "Diagonal distance" should "be accurate" in {
    val heuristic = new DiagonalDistance
    heuristic.estimateDistance(Position(0, 0), Position(0, 0)) should equal(0)
    heuristic.estimateDistance(Position(0, 0), Position(0, 1)) should equal(1)
    heuristic.estimateDistance(Position(0, 0), Position(1, 0)) should equal(1)
    heuristic.estimateDistance(Position(0, 0), Position(1, 1)) should equal(1)
    heuristic.estimateDistance(Position(5, 3), Position(2, 7)) should equal(4)
  }

  "Euclidean distance" should "be accurate" in {
    val heuristic = new EuclideanDistance
    heuristic.estimateDistance(Position(0, 0), Position(0, 0)) should equal(0)
    heuristic.estimateDistance(Position(0, 0), Position(0, 1)) should equal(1)
    heuristic.estimateDistance(Position(0, 0), Position(1, 0)) should equal(1)
    heuristic.estimateDistance(Position(0, 0), Position(1, 1)) should equal(math.sqrt(2))
    heuristic.estimateDistance(Position(3, 4), Position(0, 0)) should equal(5)
  }

  "All heuristics" should "have the correct relative sizes" in {
    val manhattan = new ManhattanDistance
    val diagonal = new DiagonalDistance
    val euclidean = new EuclideanDistance

    // when moving along an axis, all estimations should be the same
    {
      val p1 = Position(2, 2)
      val p2 = Position(4, 2)
      euclidean.estimateDistance(p1, p2) should equal(diagonal.estimateDistance(p1, p2))
      manhattan.estimateDistance(p1, p2) should equal(euclidean.estimateDistance(p1, p2))
    }

    // when moving diagonally, estimations should be different
    {
      val p1 = Position(2, 2)
      val p2 = Position(4, 4)
      euclidean.estimateDistance(p1, p2) should be >(diagonal.estimateDistance(p1, p2))
      manhattan.estimateDistance(p1, p2) should be >(euclidean.estimateDistance(p1, p2))
    }
  }
}
