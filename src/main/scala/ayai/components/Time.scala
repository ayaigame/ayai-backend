package ayai.components

import crane.Component

class Time(msActive: Int, startTime: Long ) extends Component {
  def isReady(endTime: Long): Boolean = endTime - startTime >= msActive
}
