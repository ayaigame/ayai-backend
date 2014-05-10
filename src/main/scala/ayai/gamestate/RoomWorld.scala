package ayai.gamestate

import crane.World

object RoomWorld {
  def apply(name: String, tileMap: TileMap) = new RoomWorld(name, tileMap)
}

//isLeaf is true if this RoomWorld's transports' destinations have not yet been generated
class RoomWorld(val name: String, val tileMap: TileMap) extends World
// class RoomWorld(val name: String, val tileMap: TileMap, var isLeaf) extends World
