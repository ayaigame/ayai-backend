package ayai.actions

import ayai.components.Position
import ayai.components.Velocity
import com.artemis.Entity
import ayai.actions.MoveDirection

sealed trait Action { 
 def process(e : Entity)
}

case class MovementAction(var direction : MoveDirection) extends Action {
	def process(e : Entity) {
		val position : Position = e.getComponent(classOf[Position])
		val velocity : Velocity = e.getComponent(classOf[Velocity])
		//add thread synchronization
		position.x += direction.xDirection * velocity.x
		position.y += direction.yDirection * velocity.y
	}
}
case class ItemAction(var itemAction : ItemAct) extends Action {
	def process(e : Entity) {
		//get the inventory of the character and base it off what action of the command given
	}
}
case class AttackAction(var action : AttackAct) extends Action {
	def process(e : Entity) {
		//print somethign
	}
}