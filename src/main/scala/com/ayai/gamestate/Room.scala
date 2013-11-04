package com.ayai.main.gamestate

import com.artemis.Component
import scala.collection.mutable.ArrayBuffer
import scala.util.parsing.json._

class Room {
	var map = new ArrayBuffer[ArrayBuffer[Int]]()
	var portals = new ArrayBuffer[Portal]()
  var entities = new ArrayBuffer[Component]()

  def intializeMap(mapJSON: String) = {
    val result = JSON.parseFull(mapJSON)
    println(result)
  }
}