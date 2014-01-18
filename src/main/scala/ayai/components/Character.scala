package ayai.components

import com.artemis.Component

class Character(val characterId: String) extends Component{
	  override def toString: String = {
	  	return "\"" + characterId "\""
  }
}