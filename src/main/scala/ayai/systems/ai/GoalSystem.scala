package ayai.systems

import ayai.components._
import ayai.actions._
import ayai.gamestate.RoomWorld
import ayai.maps.Tile

import crane.{Entity, System}

import scala.math.abs
import scala.collection.mutable._

object GoalSystem {
  def apply() = new GoalSystem()
}

class GoalSystem extends System {
  def getScore(current: Position, goal: Position): Int = {
    abs(current.x - goal.x) + abs(current.y - goal.y)
  }

  def getNewMove(possibleMoves: Array[Tile], goal: Position): Tile = {
    // Get all scores, sort them by score, return the tile with the lowest score
    possibleMoves.map{
      move => (move, getScore(move.tilePosition, goal))
    }.sortWith((x, y) => x._2 < y._2).head._1
  }

  def neighbors(position: Position) = (position.x, position.y) match {
    case(x, y) =>
      for { 
        dx <- -1 to 1
        dy <- -1 to 1
        if !(dx == 0 && dy == 0) && !(abs(dx) == abs(dy))
      } yield (x + dx, y + dy)
  }

  def findMoves(current: Tile, tileMap: Array[Array[Tile]]): Array[Tile] = {
    neighbors(current.indexPosition).withFilter{
      pos => tileMap.isDefinedAt(pos._2) && tileMap(pos._2).isDefinedAt(pos._1)
    }.map(pos => tileMap(pos._2)(pos._1)).filter{
      tile => !tileMap(tile.indexPosition.y)(tile.indexPosition.x).isCollidable
    }.toArray
  }

  def findDirection(entity: Entity, tp: Position): MoveDirection = {
    (entity.getComponent(classOf[Position]): @unchecked) match {
      case Some(ep: Position) =>
        val tMap = world.asInstanceOf[RoomWorld].tileMap
        val map = tMap.array
        val bestMove = getNewMove(findMoves(tMap.getTileByPosition(ep), map), tp)

        val xDistance = ep.x - bestMove.tilePosition.x
        val yDistance = ep.y - bestMove.tilePosition.y

        (abs(xDistance) > abs(yDistance), xDistance > 0, yDistance > 0) match {
          case(true, true, _) =>
            LeftDirection
          case(true, false, _) =>
            RightDirection
          case(false, _, true) =>
            UpDirection
          case(false, _, false) =>
            DownDirection
        }
      case _ =>
        new MoveDirection(0,0)
    
    }
  }

  override def process(delta: Int) {
    val entities = world.getEntitiesByComponents(classOf[Goal], classOf[Position], classOf[Actionable])
    for(entity <- entities) {
      val goal = (entity.getComponent(classOf[Goal]): @unchecked) match {
        case Some(g: Goal) => g
      }
      val direction = findDirection(entity, goal.goal.asInstanceOf[MoveTo].position)
      (entity.getComponent(classOf[Actionable]): @unchecked) match {
        case Some(actionable: Actionable) =>
        (entity.getComponent(classOf[Position]): @unchecked) match {
          case Some(ep: Position) =>

          val tp = goal.goal.asInstanceOf[MoveTo].position
          actionable.active = !(ep.x == tp.x && ep.y == tp.y)
          actionable.action = direction
        }
      }
    }
  }
}
