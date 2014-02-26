package ayai.components

/** Crane Imports **/
import crane.Component

class Transport(var startPosition: Position, 
        var toRoom: Room) extends Component {
  override def toString: String = startPosition.toString + " room id: " + toRoom.id
}
