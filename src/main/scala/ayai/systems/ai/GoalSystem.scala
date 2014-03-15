package ayai.systems

import ayai.components._
import ayai.actions._

import crane.{Entity, System}

import scala.math.abs

object GoalSystem {
  def apply() = new GoalSystem()
}

class GoalSystem extends System {
  def getScore(current : Position, goal : Position) = Int {
    val dx = abs(current.x - goal.x)
    val dy = abs(current.y - goal.y)
    val dist = dx + dy
    return dist
  }

  def getNewMove(possibleMoves : Array[Tile], goal : Position) = Tile {
    val score = 1000000
    val bestMove = null
    for(move <- possibleMoves) {
      temp = getScore(move.position, goal)
      if(temp < score){
        score = temp
        bestMove = move
      }
    }
    return bestMove
  }

  def findMoves(current : Tile, map : Array[Array[Tile]]) = Array[Tile] {
    val MinX = null
    val MaxX = null
    val MinY = null
    val MaxY = null
    val possibleMoves = Array[Tile]

    if(current.position.x - 1 >= 0){
      MinX = current.position.x - 1
    }
    if(current.position.x + 1 <= map[0].count){
      MaxX = current.position.x + 1
    }
    if(current.position.y - 1 >= 0){
      MinY = current.position.y - 1
    }
    if(current.position.y + 1 <= map.count){
      MaxY = current.position.y + 1
    }

    if(MinX != null && MaxY != null){
      if(!map[MaxY][MinX].isCollidable){
        possibleMoves.+(map[MaxY][MinX])
      }
    }
    if(MaxY != null){
      if(!map[MaxY][current.position.x].isCollidable){
        possibleMoves.+(map[MaxY][current.position.x])
      }
    }
    if(MaxX != null && MaxY != null){
      if(!map[MaxY][MaxX].isCollidable){
        possibleMoves.+(map[MaxY][MaxX])
      }
    }
    if(MinX != null){
      if(!map[current.position.y][MinX].isCollidable){
        possibleMoves.+(map[current.position.y][MinX])
      }
    }
    if(MaxX != null){
      if(!map[current.position.y][MaxX].isCollidable){
        possibleMoves.+(map[current.position.y][MaxX])
      }
    }
    if(MinX != null && MinY != null){
      if(!map[MinY][MinX].isCollidable){
        possibleMoves.+(map[MinY][MinX])
      }
    }
    if(MinY != null){
      if(!map[MinY][current.position.x].isCollidable){
        possibleMoves.+(map[MinY][current.position.x])
      }
    }
    if(MaxX != null && MinY != null){
      if(!map[MinY][MaxX].isCollidable){
        possibleMoves.+(map[MinY][MaxX])
      }
    }
    return possibleMoves
  }

  def findDirection(entity: Entity, tp: Position): MoveDirection = {
    (entity.getComponent(classOf[Position]): @unchecked) match {
      case Some(ep: Position) =>
        val tMap = world.tileMap
        val map = tMap.array
        val possibleMoves = findMoves(tMap.getTileByPosition(ep), map)
        val bestMove = getNewMove(possibleMoves, tp)
        val xDistance = ep.x - bestMove.position.x
        val yDistance = ep.y - bestMove.position.y

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
