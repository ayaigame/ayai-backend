package ayai.components

import com.artemis.Component
import java.util.Date

/*
** time : is an int  in seconds to wait to respawn
** delta : is the time that the user initially died at
*/
class Respawn(val time : Int = 15, val delta : Date) extends Component {
	def isReady(deltaTime : Date) {
		if(deltaTime - delta > 15) : Boolean = {
			true
		}
		false
	}
}