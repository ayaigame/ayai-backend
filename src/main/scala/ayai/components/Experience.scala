package ayai.components

import crane.Component

case class Experience(var baseExperience: Long, var level: Int) extends Component {
  def levelUp(experienceThreshold: Long): Boolean = {
    if(baseExperience >= experienceThreshold) {
      level += 1
      return true
    }
    return false
  }
}