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
  def getScore(current : Position, goal : Position) : Int = {
    val dx = abs(current.x - goal.x)
    val dy = abs(current.y - goal.y)
    val dist = dx + dy
    return dist
  }

  def getNewMove(possibleMoves : Array[Tile], goal : Position) : Tile = {
    var score = 1000000
    var bestMove = possibleMoves(0)
    for(move <- possibleMoves) {
      val temp = getScore(move.tilePosition, goal)
      if(temp <= score){
        score = temp
        bestMove = move
      }
    }
    return bestMove
  }

  def findMoves(current : Tile, map : Array[Array[Tile]]) : Array[Tile] = {
    val currentX = current.indexPosition.x
    val currentY = current.indexPosition.y
    var possibleMoves = new ArrayBuffer[Tile]()

    for(i <- -1 to 1){
      for(j <- -1 to 1){
        if(map.isDefinedAt(currentY+i) && map(currentY+i).isDefinedAt(currentX+j)){
          if(!(i==0 && j==0)){
            if(!map(currentY+i)(currentX+j).isCollidable){
              possibleMoves.append(map(currentY+i)(currentX+j))
            }
          }
        }
      }
    }
    return possibleMoves.toArray
  }

  def findDirection(entity: Entity, tp: Position): MoveDirection = {
    (entity.getComponent(classOf[Position]): @unchecked) match {
      case Some(ep: Position) =>
        val tMap = world.asInstanceOf[RoomWorld].tileMap
        val map = tMap.array
        val possibleMoves = findMoves(tMap.getTileByPosition(ep), map)
        val bestMove = getNewMove(possibleMoves, tp)
        val xDistance = ep.x - bestMove.tilePosition.x
        val yDistance = ep.y - bestMove.tilePosition.y

        (abs(xDistance) > abs(yDistance), xDistance > 0, yDistance > 0) match {
          case(true, true, _) =>
            LeftDirection
          case(true, false, _) =>
            RightDirection
          case(false, _ ,true) =>
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
