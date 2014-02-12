package ayai.actions


sealed class MoveDirection(val xDirection : Int, val yDirection : Int)

case class LeftDirection extends MoveDirection(-1,0) {
	def asJson() {
		("action" -> "walkleft")
	}
}
case class RightDirection extends MoveDirection(1,0) {
	def asJson() {
		("action" -> "walkright")
	}
}

case class UpDirection extends MoveDirection(0,-1){
	def asJson() {
		("action" -> "walkup")
	}
}
case class DownDirection extends MoveDirection(0,1){
	def asJson() {
		("action" -> "walkdown")
	}
}
case class UpLeftDirection extends MoveDirection(-1,-1){
	def asJson() {
		("action" -> "walkleft")
	}
}

case class UpRightDirection extends MoveDirection(1,-1){
	def asJson() {
		("action" -> "walkright")
	}
}

case class DownRightDirection extends MoveDirection(1,1) {
	def asJson() {
		("action" -> "walkright")
	}
}

case class DownLeftDirection extends MoveDirection(-1,1) {
	def asJson() {
		("action" -> "walkleft")
	}
}

