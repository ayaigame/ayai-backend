package com.ayai.main.gamestate

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import com.ayai.main.systems._
import com.artemis.World

object GameState  {
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

  def getRoomJSON(roomId: Int): String = {
    return "{}"
  }

  def getPlayerRoomJSON(playerId: Int): String = {
    return index.get(playerId) match {
        case Some(roomId: Int) => getRoomJSON(roomId)
        case _ => "{\"error\": \"No player found with id\"}" //$playerId.\"}"
        //Scala string interpolation doesn't work with the \ escape character...
      }
  }
}