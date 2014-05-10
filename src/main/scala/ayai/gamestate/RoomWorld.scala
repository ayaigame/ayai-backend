package ayai.gamestate

import crane.World

object RoomWorld {
  def apply(name: String, tileMap: TileMap, isLeaf: Boolean) = new RoomWorld(name, tileMap, isLeaf)
}

//isLeaf is true if this RoomWorld's transports' destinations have not yet been generated
class RoomWorld(val name: String, val tileMap: TileMap, var isLeaf: Boolean) extends World
