package ayai.components

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

trait ItemType {
	def asJson(): JObject
}
