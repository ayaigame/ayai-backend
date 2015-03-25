package ayai.components

/** Crane Imports **/

import ayai.gamestate.TileMap
import crane.{Component, Entity}

import scala.collection.mutable.ArrayBuffer
import math.floor

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class SenseComponent extends Component {
  def notifySystem() = {
    //...
  }
}

class Hearing extends SenseComponent {
  var hearingAbility : Int = -1
}

class SoundProducing extends Component {
  var intensity : Int = -1
}

class SoundEntity extends Entity {
  var intensity : Int = -1
}

class Vision( var los : LOS ) extends SenseComponent {
  var visionRange : Int = -1

  def drawLine(start: Position, end : Position, bounds: Bounds, tileMap: TileMap): Boolean = {
    return los.drawLine(start, end, bounds, tileMap)
  }

}

class LOS {
  def drawLine(start: Position, end : Position, bounds: Bounds, tileMap: TileMap): Boolean = {
    false
  }

  def isCollision(position: Position, bounds: Bounds, tileMap: TileMap): Boolean = {
    tileMap.isPositionInBounds(position)

    //if on tile Collision go back to original position
    val collision = tileMap.onTileCollision(position, bounds)
    if (collision) {
      true
    }
    else {
      false
    }
  }
}

class BresenhamLOS extends LOS {
  override def drawLine(start: Position, end : Position, bounds: Bounds, tileMap: TileMap): Boolean = {
    val signx = if (start.x < end.x) 1 else -1
    val signy = if (start.y < end.y) 1 else -1
    var deltax = math.abs(end.x - start.x)
    val deltay = math.abs(end.y - start.y)
    if (deltax == 0) deltax = 1 // avoid divide by zero errors
    var error: Double = 0.0
    val deltaerr: Double = math.abs(deltay / deltax)

    var yIter: Int = start.y
    var xIter: Int = start.x
    var result: Boolean = true
    while (xIter != end.x) {
      if (isCollision(new Position(xIter, yIter), bounds, tileMap)) {
        result = false
      }
      error = error + deltaerr
      while (error >= 0.5) {
        if (isCollision(new Position(xIter, yIter), bounds, tileMap)) {
          result = false
        }
        yIter = yIter + signy
        error = error - 1.0
      }
      xIter = xIter + signx
    }
    result
  }
}

class WuLOS extends LOS {
  override def drawLine(start: Position, end : Position, bounds: Bounds, tileMap: TileMap): Boolean = {
    val steep = math.abs(end.y - start.y) > math.abs(end.x - start.x)
    val (p3, p4) = if (steep) (end, start) else (start, end)
    val (a, b) = if (p3.x > p4.x) (p4, p3) else (p3, p4)
    var deltax = b.x - a.x
    val deltay = b.y - a.y
    if (deltax == 0) deltax = 1 // avoid divide by zero errors
    val gradient = deltay / deltax
    var intersection = 0.0
    var result = true
    var endpointResult = true

    var xpixel1 = math.round(a.x);
    {
      val yend = a.y + gradient * (xpixel1 - a.x)
      val xgap = rearFractional(a.x + 0.5)
      endpointResult = endpoint(xpixel1, yend, steep, bounds, tileMap)
      if (!endpointResult) {result = false}
      intersection = yend + gradient
    }

    val xpixel2 = math.round(b.x);
    {
      val yend = b.y + gradient * (xpixel2 - b.x)
      val xgap = fractional(b.x + 0.5)
      endpointResult = endpoint(xpixel2, yend, steep, bounds, tileMap)
      if (!endpointResult) {result = false}
    }

    for (x <- (xpixel1 + 1) to (xpixel2 - 1)) {
      if (steep) {
        if (isCollision(new Position(intersection.toInt, x), bounds, tileMap)) { result = false } //rearFractional(intersection))
        if (isCollision(new Position(intersection.toInt+1, x),  bounds, tileMap)) { result = false } //fractional(intersection))
      } else {
        if (isCollision(new Position(x, intersection.toInt), bounds, tileMap)) { result = false } // rearFractional(intersection))
        if (isCollision(new Position(x, intersection.toInt+1), bounds, tileMap)) { result = false } // fractional(intersection))
      }
      intersection = intersection + gradient
    }

    result
  }

  def endpoint(xpixel: Double, yend: Double, steep: Boolean, bounds : Bounds, tileMap : TileMap): Boolean = {
    val ypixel = floor(yend)
    var result = true
    if (steep) {
      if (isCollision(new Position(ypixel.toInt, xpixel.toInt), bounds, tileMap)) { result = false } //rearFractional(yend) * xgap)
      if (isCollision(new Position(ypixel.toInt+1, xpixel.toInt), bounds, tileMap)) { result = false } //fractional(yend) * xgap)
    } else {
      if (isCollision(new Position(xpixel.toInt, ypixel.toInt), bounds, tileMap)) { result = false } //rearFractional(yend) * xgap)
      if (isCollision(new Position(xpixel.toInt, ypixel.toInt+1), bounds, tileMap)) { result = false } //fractional(yend) * xgap)
    }
    result
  }

  def fractional(x: Double) = x - floor(x)
  def rearFractional(x: Double) = 1 - fractional(x)
  def average(a: Float, b: Float) = (a + b) / 2
}

class MemoryContents {
  var entityID: Int = -1
  var entityPosition: Option[Position] = None
  var relationship: Int = -1
}

class Memory extends SenseComponent {
  var memoryAbility: Int = -1
  var entitiesRemembered: ArrayBuffer[MemoryContents] = new ArrayBuffer[MemoryContents]()
}