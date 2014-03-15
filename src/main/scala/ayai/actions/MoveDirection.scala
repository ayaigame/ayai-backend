package ayai.actions

/** Ayai Imports **/
import ayai.components._

/** Crane Imports **/
import crane.Entity

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class MoveDirection(val xDirection: Int, val yDirection: Int) extends Action {
  override def process(e: Entity) {
    (e.getComponent(classOf[Position]),
      e.getComponent(classOf[Velocity])) match {
      case(Some(position: Position), Some(velocity: Velocity)) =>
        position.x += xDirection * velocity.x
        position.y += yDirection * velocity.y
      case _ => return
    }
  }

  def asJson(): JObject =  { ("action" -> "empty")}
}

case object LeftDirection extends MoveDirection(-1,0) {
  override def asJson(): JObject =  {
    ("action" -> "walkleft")
  }
}
case object RightDirection extends MoveDirection(1,0) {
  override def asJson(): JObject =  {
    ("action" -> "walkright")
  }
}

case object UpDirection extends MoveDirection(0,-1){
  override def asJson(): JObject =  {
    ("action" -> "walkup")
  }
}
case object DownDirection extends MoveDirection(0,1){
  override def asJson() : JObject = {
    ("action" -> "walkdown")
  }
}
case object UpLeftDirection extends MoveDirection(-1,-1){
  override def asJson(): JObject =  {
    ("action" -> "walkleft")
  }
}

case object UpRightDirection extends MoveDirection(1,-1){
  override def asJson(): JObject =  {
    ("action" -> "walkright")
  }
}

case object DownRightDirection extends MoveDirection(1,1) {
  override def asJson(): JObject =  {
    ("action" -> "walkright")
  }
}

case object DownLeftDirection extends MoveDirection(-1,1) {
  override def asJson(): JObject =  {
    ("action" -> "walkleft")
  }
}

