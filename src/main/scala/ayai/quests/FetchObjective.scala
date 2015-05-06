package ayai.quests

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class FetchObjective(name: String, target: String, recipient: String) extends Objective(name) {
  override def asJson(): JObject = {
    ("name" -> name) ~
      ("target" -> target) ~
      ("recipient" -> recipient)
  }


  override def isComplete: Boolean = {
    // do something fancy here to check if the target has been delivered.
    false
  }
}