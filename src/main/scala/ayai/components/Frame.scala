package ayai.components

import crane.Component

class Frame(framesActive : Int,var frameCounts : Int) extends Component {
  def isReady() : Boolean = {
    if(framesActive <= frameCounts) {
      return true
    }
    return false
  }
}