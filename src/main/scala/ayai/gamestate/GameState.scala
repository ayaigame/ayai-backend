package ayai.gamestate

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import ayai.components._
import com.artemis.World
import com.artemis.Entity

object GameState  {
  // Stores all the rooms. rooms(roomId) will return the correct room.
  var rooms = new ArrayBuffer[Room]()

  /*
  * Each player will get an entry in the index so it is fast and easy to look
  * up what room they are in.
  * I'm not sure we want to use a HashMap because:
  * "Hash tables are thus very fast so long as the objects placed in them have
  * a good distribution of hash codes."
  * and I don't think our playerIds will have a good distribution of hash codes.
  */
  var index = new HashMap[Int, Int]()
  var nextRoomId = 0
  var nextPlayerId = 0

  def getPlayerRoomJSON(playerId: Int): String = {
    return index.get(playerId) match {
        case Some(roomId: Int) => rooms(roomId).jsonify()
        case _ => "{\"error\": \"No player found with id " + playerId.toString() + ".\"}"
        //Scala string interpolation doesn't work with the \ escape character...
      }
  }

  def createRoom(mapJSON: String): Room = {
    val newRoom = new Room(nextRoomId, mapJSON)
    rooms += newRoom
    nextRoomId = nextRoomId + 1
    return newRoom
  }

  def getNextPlayerId(): Int = {
    val temp = nextPlayerId
    nextPlayerId = nextPlayerId + 1
    return temp
  }

  def addPlayer(roomId: Int, player: Entity) = {
    index += (player.getComponent(classOf[Player]).playerId -> roomId)
    rooms(roomId).addEntity(player)
  }
}
