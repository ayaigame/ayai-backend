package ayai.components

import crane.Component
import java.util.Date

/*
** time : is an int  in seconds to wait to respawn
** delta : is the time that the user initially died at
*/
class Respawn(val time : Int = 1500, val delta : Long) extends Component {
	def isReady(deltaTime : Long ) : Boolean = {
		if(deltaTime - delta > time) {
			true
		}
		false
	}
}