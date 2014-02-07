package ayai.components

import crane.Component
import ayai.components.Position
import ayai.components.Room

class Transport(var startPosition : Position, 
				var toRoom : Room) extends Component {
	override def toString : String = startPosition.toString + " room id: " + toRoom.id
}