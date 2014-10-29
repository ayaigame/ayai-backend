package ayai.quests

import net.liftweb.json._

abstract class Objective(name: String) {
	def asJson: JObject
}