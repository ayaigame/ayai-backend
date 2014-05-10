package ayai.components

import ayai.gamestate._

/** Crane Imports **/
import crane.Component

class Transport(var startPosition: Position, var toRoom: RoomWorld) extends Component {
  override def toString: String = startPosition.toString + " room id: " + toRoom.id
}
