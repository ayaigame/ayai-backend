package ayai.gamestate

import crane.World

object RoomWorld {
  def apply(name: String, val tileMap: TileMap) = new RoomWorld(name, tileMap)
}

class RoomWorld(val name: String, val tileMap: TileMap) extends World
