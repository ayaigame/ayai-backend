package ayai.components

import crane.Component
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import scala.collection.mutable.ArrayBuffer

case class SpriteSheet( path: String) extends Component {
	def asJson: JObject = {
		("spritesheet" -> 
			("path" -> path))
	}
}