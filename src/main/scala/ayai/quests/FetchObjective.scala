package ayai.quests

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class FetchObjective( target: String, recipient: String) extends Objective() {
  override def asJson(): JObject = {
      ("target" -> target) ~
      ("recipient" -> recipient)
  }


  override def isComplete: Boolean = {
    // do something fancy here to check if the target has been delivered.
    false
  }
}