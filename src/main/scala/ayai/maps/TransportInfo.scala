package ayai.maps

/** Ayai Imports **/
import ayai.components.Position

class TransportInfo(var startingPosition: Position,
                    var endingPosition: Position,
                    var fromRoomId: Int,
                    var toRoomId: Int,
                    var toPosition: Position,
                    var direction: String) {
}
