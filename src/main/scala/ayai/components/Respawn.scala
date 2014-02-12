package ayai.components

import crane.Component

/*
** time : is an int  in seconds to wait to respawn
** delta : is the time that the user initially died at
*/
class Respawn(val time : Int = 15, val delta : Long) extends Component {
	def isReady(deltaTime : Long) : Boolean = {

		if(deltaTime - delta > time) {
			return true
		} else {
			return false
		}
	}
}