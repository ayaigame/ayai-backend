package ayai.factories

import ayai.gamestate.RoomWorld
import ayai.components.{Position, Bounds}

// Merges tilemap and world into a traversable graph for AI purposes

object NodeState extends Enumeration {
  type NodeState = Value
  val IMPASS, SLOW, NORMAL = Value
}

import NodeState.{NodeState, IMPASS, NORMAL, SLOW}

case class Node(val state: NodeState) {
  override def toString = s"$printableState Node"

  private def printableState = state match {
    case IMPASS => "Impassable"
    case NORMAL => "Normal"
    case SLOW => "Slow"
    case _ => "Unknown"
  }
}

object GraphFactory {
  def generateGraph(world: RoomWorld): Array[Array[Node]] = {
    val tileMap = world.tileMap
    val entities = world.getEntitiesByComponents(classOf[Position], classOf[Bounds])

    // start by assuming all nodes are passable
    val entityArray = Array.fill[Node](tileMap.width, tileMap.height) { Node(NORMAL) }

    // mark locations where an entity is standing as impassable
    entities.foreach { entity =>
      val position = entity.getComponent(classOf[Position])
      (position: @unchecked) match {
        case Some(p: Position) =>
          val gridPosition = convertPositionToGrid(p, tileMap.tileSize.toFloat)
          if(inBounds(gridPosition._1, gridPosition._2))
            entityArray(gridPosition._1)(gridPosition._2) = Node(IMPASS)
      }
    }

    val finalMap = tileMap.array.zipWithIndex.map(e => e._1 zip entityArray(e._2))

    finalMap.map{ row => row.map{ case (tile, node) =>
      // Treat grid locations as impassable if either they were marked impassable earlier or its tile is impassable.
      // Otherwise, assume they are normal.
      // (NodeStates other than IMPASS and NORMAL are currently not looked for and created.)
      (node.state, tile.isCollidable) match {
        case (IMPASS, _) =>
          Node(IMPASS)
        case (_, true) =>
          Node(IMPASS)
        case _ =>
          Node(NORMAL)
      }
    }}
  }

  private def convertPositionToGrid(position: Position, ratio: Float): Tuple2[Int, Int] =  {
    (math.round((position.x / ratio)), math.round((position.y / ratio)))
  }

  private def inBounds(max: Int, indexes: Int*): Boolean = {
    indexes.forall(Range.inclusive(0, max).contains)
  }
}
