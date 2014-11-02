package ayai.components

import crane.Component

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

case class Animation(name: String, startFrame: Int, endFrame: Int) extends Component {
	def asJson: JObject = {
    ("name" -> name) ~
      ("startFrame" -> startFrame) ~
      ("endFrame" -> endFrame)
  }
}