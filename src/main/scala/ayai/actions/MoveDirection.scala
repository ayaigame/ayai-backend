package ayai.actions

import crane.Entity
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import ayai.components._

class MoveDirection(val xDirection : Int, val yDirection : Int) extends Action {
	override def process(e : Entity) {
		(e.getComponent(classOf[Position]),
			e.getComponent(classOf[Velocity])) match {
			case(Some(position : Position), Some(velocity : Velocity)) =>
				position.x += xDirection * velocity.x
				position.y += yDirection * velocity.y

			case _ => return
		}
	}

	def asJson() : JObject =  { ("action" -> "empty")}
}

case class LeftDirection extends MoveDirection(-1,0) {
	override def asJson(): JObject =  {
		("action" -> "walkleft")
	}
}
case class RightDirection extends MoveDirection(1,0) {
	override def asJson(): JObject =  {
		("action" -> "walkright")
	}
}

case class UpDirection extends MoveDirection(0,-1){
	override def asJson(): JObject =  {
		("action" -> "walkup")
	}
}
case class DownDirection extends MoveDirection(0,1){
	override def asJson() : JObject = {
		("action" -> "walkdown")
	}
}
case class UpLeftDirection extends MoveDirection(-1,-1){
	override def asJson(): JObject =  {
		("action" -> "walkleft")
	}
}

case class UpRightDirection extends MoveDirection(1,-1){
	override def asJson(): JObject =  {
		("action" -> "walkright")
	}
}

case class DownRightDirection extends MoveDirection(1,1) {
	override def asJson(): JObject =  {
		("action" -> "walkright")
	}
}

case class DownLeftDirection extends MoveDirection(-1,1) {
	override def asJson(): JObject =  {
		("action" -> "walkleft")
	}
}

