package ayai.maps

import scala.collection.mutable.ListBuffer
import ayai.maps.Layer


/**
** value is the sprite id that we need to load on the frontend
** layer are 
**/
class Tile(val layers : ListBuffer[Layer]) {
	def isCollidable() : Boolean = {
		for(layer <- layers) layer match {
			case CollidableLayer(x: Int) => return true
		}
		false
	}

	def add(layer : Layer) {
		layers:+layer
	}
}