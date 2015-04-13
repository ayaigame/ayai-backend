package ayai.quests

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

// this may be an issue. I didn't realize that Position was a component, I thought it was a data structure. I'll look into this.
/*
class EscortObjective(name: String, target: String, startLocation: Position, endLocation: Position) extends Objective(name) {
  override def asJson(): JObject = {
    ("name" -> name) ~
      ("target" -> target) ~
      ("recipient" -> recipient)
  }

  override def isComplete(): Boolean = {
    // do something fancy here to check if the target has been delivered.
    return false
  }
}
*/