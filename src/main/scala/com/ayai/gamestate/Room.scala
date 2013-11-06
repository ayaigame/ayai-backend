package com.ayai.main.gamestate

import com.artemis.Entity
import scala.collection.mutable.ArrayBuffer
import scala.util.parsing.json._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class Room(roomId: Int, mapJSON: String) {
	var doubleGameMap: List[List[Double]] = JSON.parseFull(mapJSON) match {
    case Some(e: List[List[Double]]) => e
    case _ => {
      println("Error: map parsing error.")
      List(List(0))
    }
  }

  var gameMap = doubleGameMap.map((l: List[Double]) => l.map((d: Double) => d.toInt))

	var portals = new ArrayBuffer[Portal]()
  var entities = new ArrayBuffer[Entity]()

  def getRoomId(): Int = {
    return roomId
  }

  def addEntity(entity: Entity) = {
    entities += entity
  }

  def jsonify(): String = {
    compact(render(gameMap.map((l: List[Int]) => compact(render(l)))))
  }
}
