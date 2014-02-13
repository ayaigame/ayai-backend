package ayai.components
/** Crane Imports **/
import crane.{Component, Entity}

class Bullet(var initiator : Entity, var damage : Int) extends Component {
  var victim : Entity = _ 
}
