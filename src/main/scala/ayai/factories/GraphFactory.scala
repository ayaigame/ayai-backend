package ayai.factories

import ayai.gamestate.RoomWorld
import ayai.components.{Position, Bounds}

// Merges tilemap and world into a traverseable graph for AI purposes

object NodeState extends Enumeration {
  type NodeState = Value
  val IMPASS, SLOW, NORMAL = Value
}

import NodeState.{NodeState, IMPASS, NORMAL}

object Node {
  def apply(state: NodeState) = new Node(state)
  }

class Node(val state: NodeState) {
  def printableState = state match { case IMPASS => "Impassable" case NORMAL => "Normal" case _ => "Unknown" }
  override def toString = s"Node $printableState"
}

object GraphFactory {

  def convertPositionToGrid(position: Position, ratio: Float): Tuple2[Int, Int] =  {
    (math.round((position.x / ratio)), math.round((position.y / ratio)))
  }

  def generateGraph(world: RoomWorld): Array[Array[Node]] = {
    val tileMap = world.tileMap
    val entities = world.getEntitiesByComponents(classOf[Position], classOf[Bounds])

    val entityArray = Array.fill[Node](tileMap.width, tileMap.height) { Node(NORMAL)  }

    entities.foreach { entity =>
      val position = entity.getComponent(classOf[Position])
      (position: @unchecked) match {
        case Some(p: Position) =>
          val gridPosition = convertPositionToGrid(p, tileMap.tileSize.toFloat)
          entityArray(gridPosition._1)(gridPosition._2) = Node(IMPASS)
      }
    }

    val finalMap = tileMap.array.zipWithIndex.map(e => e._1 zip entityArray(e._2))

    finalMap.map{ row => row.map{ grid => 
      val tile = grid._1
      val node = grid._2
      (node.state, tile.isCollidable)  match {
        case (IMPASS, _) =>
          Node(IMPASS)
        case (_, true) =>
          Node(IMPASS)
        case _ =>
          Node(NORMAL)
      }}}
  }
  
}
