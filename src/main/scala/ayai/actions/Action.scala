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