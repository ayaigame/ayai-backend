package ayai.components

/** Crane Imports **/
import crane.Component

class Velocity (var x: Int, var y: Int) extends Component {
	var modifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
}
