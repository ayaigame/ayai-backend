package ayai.components

import crane.Component
import java.util.Date

/*
** time : is an int  in seconds to wait to respawn
** delta : is the time that the user initially died at
*/
class Respawn(val time : Int = 15, val delta : Date) extends Component {
	def isReady(deltaTime : Date) : Boolean = {
		if(deltaTime - delta > 15) {
			true
		}
		false
	}
}