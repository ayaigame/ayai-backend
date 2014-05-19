package ayai.maps

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class Tileset(val image:String, val name: String, val height: Int, val width: Int) {
	implicit def asJson(): JObject = {
		("firstgid" -> 1) ~
    ("image" -> image) ~
    ("imageheight" -> width) ~
    ("imagewidth" -> height) ~
    ("margin" -> 0) ~
    ("name" -> name) ~
    ("properties" -> null) ~
    ("spacing" -> 0) ~
    ("tileheight" -> 32) ~
    ("tilewidth" -> 32) ~
    ("transparentcolor" -> "#ff00cc")
	}

	// override def toString : String = return sets.toString
}
