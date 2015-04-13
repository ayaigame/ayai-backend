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

  "Manhattan and Diagonal heuristics" should "have the correct relative sizes" in {
    val manhattan = new ManhattanDistance
    val diagonal = new DiagonalDistance

    // when moving along an axis, estimations should be the same
    {
      val p1 = Position(2, 2)
      val p2 = Position(4, 2)
      manhattan.estimateDistance(p1, p2) should equal(diagonal.estimateDistance(p1, p2))
    }

    // when moving diagonally, estimations should be different
    {
      val p1 = Position(2, 2)
      val p2 = Position(4, 4)
      manhattan.estimateDistance(p1, p2) should be >(diagonal.estimateDistance(p1, p2))
    }
  }

  "Euclidean distance" should "have the correct scale relative to itself" in {
    val heuristic = new EuclideanDistance

    heuristic.estimateDistance(Position(0, 0), Position(0, 0)) should equal(0)

    // the same x distance is counted the same
    {
      val distance1 = heuristic.estimateDistance(Position(0, 0), Position(1, 0))
      val distance2 = heuristic.estimateDistance(Position(1, 0), Position(2, 0))
      distance1 should equal(distance2)
    }

    // the same y distance is counted the same
    {
      val distance1 = heuristic.estimateDistance(Position(0, 0), Position(0, 1))
      val distance2 = heuristic.estimateDistance(Position(0, 1), Position(0, 2))
      distance1 should equal(distance2)
    }

    // 45° diagonal distances are multiples of each other
    {
      val distance1 = heuristic.estimateDistance(Position(0, 0), Position(1, 1))
      val distance2 = heuristic.estimateDistance(Position(0, 0), Position(2, 2))
      val expectedScaleDifference = 2
      (distance1 * expectedScaleDifference) should equal(distance2)
    }
  }
}
