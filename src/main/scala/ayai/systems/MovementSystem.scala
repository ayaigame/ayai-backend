package ayai.systems

import com.artemis.annotations.Mapper
import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.Entity
import com.artemis.systems.EntityProcessingSystem

import java.util.Map

import ayai.actions._
import ayai.components.Movable
import ayai.components.Position
import ayai.components.Velocity
import ayai.maps.GameMap


class MovementSystem(map : GameMap, a: Aspect = Aspect.getAspectFor(classOf[Position], classOf[Velocity],classOf[Movable])) extends EntityProcessingSystem(a) {    
  @Mapper
  var positionMapper: ComponentMapper[Position] = _
  @Mapper
  var velocityMapper: ComponentMapper[Velocity] = _
  @Mapper 
  var movingMapper : ComponentMapper[Movable] = _
  
  	  //this will only move characters who have received a movement key and the current component is still set to True
      override def process(e: Entity) {
      	var movable : Movable =  movingMapper.get(e) 
      	var position : Position = positionMapper.get(e)
      	var velocity : Velocity = velocityMapper.get(e)

      	//if moving then process for the given direction
      	if(movable.moving) {
      		var direction : MoveDirection = movable.direction
      		var movement : MovementAction = new MovementAction(direction)
      		movement.process(e)
      	}
      }

                //p.x += velocity.speed*world.delta
        //p.y += velocity.speed*world.delta
}