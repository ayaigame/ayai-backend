package ayai.components

import com.artemis.Component

class Character(val id: String) extends Component{
	  override def toString: String = {
	  	return "\"" + id + "\""
  }
}