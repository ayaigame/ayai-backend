package ayai.components

import com.artemis.Component

class Bounds (var width: Int, var height: Int) extends Component {

	def getWidth() : Int = {
		width
	}

	def getHeight() : Int = {
		height
	}
}
