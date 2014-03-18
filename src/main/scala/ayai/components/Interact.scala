package ayai.components

import crane.Component
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
case class Interact extends Component {
	def asJson(): JObject = {
		("interact" -> "quest")
	}
}