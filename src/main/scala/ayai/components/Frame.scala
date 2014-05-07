package ayai.components

import crane.Component

class Frame(var framesActive: Int) extends Component {
  def isReady: Boolean = framesActive == 0
}
