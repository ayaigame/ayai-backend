package ayai.quests

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class KillObjective(name: String, totalComplete: Int, totalNeeded:Int) extends Objective(name) {
	override def asJson(): JObject = {
		("name" -> name) ~
		("totalComplete" -> totalComplete) ~
		("totalNeeded" -> totalNeeded)
	}
}