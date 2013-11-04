package com.ayai.main.gamestate

import com.artemis.Entity
import scala.collection.mutable.ArrayBuffer
import scala.util.parsing.json._

class Room(roomId: Int) {
	var map = new ArrayBuffer[ArrayBuffer[Int]]()
	var portals = new ArrayBuffer[Portal]()
  var entities = new ArrayBuffer[Entity]()

  def getRoomId(): Int = {
    return roomId
  }

  def intializeMap(mapJSON: String) = {
    val result = JSON.parseFull(mapJSON)
    println(result)
  }

  def addEntity(entity: Entity) = {
    entities += entity
  }

  def jsonify(): String = {
    return "{}"
  }
}
