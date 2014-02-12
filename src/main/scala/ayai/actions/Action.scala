package ayai.actions

import ayai.components.Position
import ayai.components.Velocity
import crane.Entity
import ayai.actions.MoveDirection

sealed trait Action { 
 def process(e : Entity)
}

case class MovementAction(var direction : MoveDirection) extends Action {
	def process(e : Entity) {
		(e.getComponent(classOf[Position]),
			e.getComponent(classOf[Velocity])) match {
			case(Some(position : Position), Some(velocity : Velocity)) =>
				position.x += direction.xDirection * velocity.x
				position.y += direction.yDirection * velocity.y

			case _ => return
		}
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