package ayai.components

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

trait ItemType {
	def asJson(): JObject
	def copy(): ItemType
}

case class Consumable(val name: String = "") extends ItemType {
	def asJson(): JObject = {
		("name" -> name)
	}

	def copy(): ItemType = {
		return new Consumable(name)
	}
}