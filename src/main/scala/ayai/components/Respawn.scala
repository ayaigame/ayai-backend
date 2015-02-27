package ayai.components

/** Crane Imports **/
import crane.Component

/** Respawn class
* @param time : is an int  in seconds to wait to respawn
* @param delta : is the time that the user initially died at
*/
class Respawn(val time: Int = 1500, val delta: Long) extends Component {
  def isReady(deltaTime: Long): Boolean = deltaTime - delta > time
  def timeLeft(deltaTime: Long): Int = (deltaTime - delta).toInt
}
