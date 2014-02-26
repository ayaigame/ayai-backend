package ayai.maps

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class Tilesets(sets: List[String]) {
	implicit def asJson(): JObject = {
		("tilesets" -> sets.map{ s =>
			("image" -> s)})
	}

	override def toString : String = return sets.toString
}
