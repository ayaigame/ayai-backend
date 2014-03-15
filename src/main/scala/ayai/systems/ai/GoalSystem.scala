package ayai.systems

import ayai.components._
import ayai.actions._
import ayai.gamestate.RoomWorld
import ayai.maps.Tile

import crane.{Entity, System}

import scala.math.abs

object GoalSystem {
  def apply() = new GoalSystem()
}

class GoalSystem extends System {
  //def getScore(current: Position, goal: Position): Int = {
  //  val dx = abs(current.x - goal.x)
  //  val dy = abs(current.y - goal.y)
  //  val dist = dx + dy
  //  return dist
  //}

  //def getNewMove(possibleMoves: Array[Tile], goal: Position): Tile = {
  //  var score = 1000000
  //  var bestMove: Tile = new Tile(null, null, null)
  //  for(move <- possibleMoves) {
  //    val temp = getScore(move.indexPosition, goal)
  //    if(temp < score){
  //      score = temp
  //      bestMove = move
  //    }
  //  }
  //  return bestMove
  //}

  //def findMoves(current: Tile, map: Array[Array[Tile]]): Array[Tile] = {
  //  var MinX: Int = -1
  //  var MaxX: Int = -1
  //  var MinY: Int = -1
  //  var MaxY: Int = -1
  //  var possibleMoves = Array[Tile]()

  //  if(current.indexPosition.x - 1 >= 0){
  //    MinX = current.indexPosition.x - 1
  //  }
  //  if(current.indexPosition.x + 1 <= map(0).length){
  //    MaxX = current.indexPosition.x + 1
  //  }
  //  if(current.indexPosition.y - 1 >= 0){
  //    MinY = current.indexPosition.y - 1
  //  }
  //  if(current.indexPosition.y + 1 <= map.length){
  //    MaxY = current.indexPosition.y + 1
  //  }

  //  if(MinX != -1 && MaxY != -1){
  //    if(!map(MaxY)(MinX).isCollidable){
  //      //possibleMoves += map(MaxY)(MinX)
  //    }
  //  }
  //  if(MaxY != -1){
  //    if(!map(MaxY)(current.indexPosition.x).isCollidable){
  //      //possibleMoves += map(MaxY)(current.indexPosition.x) 
  //    }
  //  }
  //  if(MaxX != -1 && MaxY != -1){
  //    if(!map(MaxY)(MaxX).isCollidable){
  //      //possibleMoves += map(MaxY)(MaxX) 
  //    }
  //  }
  //  if(MinX != -1){
  //    if(!map(current.indexPosition.y)(MinX).isCollidable){
  //      //possibleMoves += map(current.indexPosition.y)(MinX) 
  //    }
  //  }
  //  if(MaxX != -1){
  //    if(!map(current.indexPosition.y)(MaxX).isCollidable){
  //      //possibleMoves += map(current.indexPosition.y)(MaxX) 
  //    }
  //  }
  //  if(MinX != -1 && MinY != -1){
  //    if(!map(MinY)(MinX).isCollidable){
  //      //possibleMoves += map(MinY)(MinX) 
  //    }
  //  }
  //  if(MinY != -1){
  //    if(!map(MinY)(current.indexPosition.x).isCollidable){
  //      //possibleMoves += map(MinY)(current.indexPosition.x) 
  //    }
  //  }
  //  if(MaxX != -1 && MinY != -1){
  //    if(!map(MinY)(MaxX).isCollidable){
  //      //possibleMoves += map(MinY)(MaxX)
  //    }
  //  }
  //  return possibleMoves
  //}

  def findDirection(entity: Entity, tp: Position): MoveDirection = {
    (entity.getComponent(classOf[Position]): @unchecked) match {
      case Some(ep: Position) =>
        val xDistance = ep.x - tp.x
        val yDistance = ep.y - tp.y

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
