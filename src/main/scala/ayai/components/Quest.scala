package ayai.components

import crane.Component
import ayai.quests.{Objective}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class Quest(title: String, description: String, recommendLevel: Int, objective: Objective) extends Component {

	def asJson(): JObject = {
		("title" -> title) ~
		("description" -> description) ~
		("recommendLevel" -> recommendLevel) ~
		("objective" -> objective.asJson)
	}	
}