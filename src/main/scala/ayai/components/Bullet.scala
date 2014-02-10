package ayai.components

import crane.Component
import crane.Entity

class Bullet(var initiator : Entity, var damage : Int) extends Component {
	var victim : Entity = _ 
}