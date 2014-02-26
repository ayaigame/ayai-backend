package ayai.gamestate

import crane.World

object RoomWorld {
  def apply(name: String) = new RoomWorld(name)
}

class RoomWorld(val name: String) extends World
