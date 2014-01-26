package ayai.components

import com.artemis.Component
import com.artemis.Entity
import ayai.components.Position

class Character(val characterId: String, x : Int, y : Int) extends Component{
	val currentPosition = new Position(x, y)

	override def toString: String = {
		return "\"" + characterId + "\""
  	}
}