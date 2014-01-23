package ayai.components

import com.artemis.Component
import com.artemis.Entity

class Character(val characterId: String) extends Component{
	override def toString: String = {
		return "\"" + characterId + "\""
  	}
}