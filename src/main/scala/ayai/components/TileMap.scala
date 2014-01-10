package ayai.components

import com.artemis.Component
import ayai.maps.Tile
import ayai.maps.Layer

import ayai.components.Position

//128 x 128 is only default
class TileMap(val array : Array[Array[Tile]] = Array.fill[Tile](128,128)(new Tile(0)), val tileSize : Int = 32) extends Component {

	//getMaximumPosition - get the maximum position value for x
	def getMaximumWidth() : Int = {
		return array.length * tileSize
	}

	//getMaximumHeight - get the maximum position value for y
	def getMaximumHeight() : Int = {
		return (array(0).length * tileSize)
	}

	
	def getTileByPosition(position : Position) : Tile = {
		val x : Int = position.x
		val y : Int = position.y

		return array(valueToTile(x))(valueToTile(y))

	}

	def valueToTile(value : Int) : Int = {
		return (value / array.length)
	}
}