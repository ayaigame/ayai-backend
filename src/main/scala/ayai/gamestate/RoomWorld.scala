package ayai.gamestate

import crane.{Entity, World}

object RoomWorld {
  def apply(id: Int, tileMap: TileMap, isLeaf: Boolean) = new RoomWorld(id, tileMap, isLeaf)
}

//isLeaf is true if this RoomWorld's transports' destinations have not yet been generated
class RoomWorld(val id: Int, val tileMap: TileMap, var isLeaf: Boolean) extends World
