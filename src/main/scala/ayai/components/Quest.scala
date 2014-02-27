package ayai.components

import crane.Component
import ayai.quests.{Objective}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class Quest(var id: Int, var title: String, var description: String, var recommendLevel: Int, var objective: Objective) extends Component {

	def asJson(): JObject = {
		("id" -> id) ~
		("title" -> title) ~
		("description" -> description) ~
		("recommendLevel" -> recommendLevel) ~
		("objective" -> objective.asJson)
	}
}