package ayai.components

import crane.Component
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import scala.collection.mutable.ArrayBuffer

case class SpriteImage( path: String, animations: ArrayBuffer[Animation], xsize: Int, ysize: Int ) extends Component {
	def asJson: JObject = {
		("spritesheet" -> 
			("path" -> path) ~
			("animations" -> animations.map{a =>a.asJson}))
			("xsize" -> xsize) ~
			("ysize" -> ysize)
	}
}