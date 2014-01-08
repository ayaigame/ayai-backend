package ayai.components

import com.artemis.Component
import com.artemis.Entity

class Bullet(var initiator : Entity, var damage : Int) extends Component {
	var victim : Entity = _ 
}