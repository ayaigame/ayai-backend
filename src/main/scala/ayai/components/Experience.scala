package ayai.components

import crane.Component
import ayai.apps.Constants

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Experience(var baseExperience: Long, var level: Int) extends Component {
  def levelUp(experienceThreshold: Long): Boolean = {
    if(baseExperience >= experienceThreshold) {
      level += 1
      return true
    }
    return false
  }

  def asJson() : JObject = {
    ("experience" -> 
      ("baseExperience" -> baseExperience) ~
      ("maxExperience" -> Constants.EXPERIENCE_ARRAY(level-1)) ~
      ("level" -> level))
  }
}