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
import ayai.components.Health
import ayai.components.Room
import ayai.components.TileMap
import scala.collection.mutable.HashMap

class MovementSystem(roomHash : HashMap[Int, Entity], a: Aspect = Aspect.getAspectForAll(classOf[Position], classOf[Velocity],classOf[Movable], classOf[Room])) extends EntityProcessingSystem(a) {    
  @Mapper
  var positionMapper: ComponentMapper[Position] = _
  @Mapper
  var velocityMapper: ComponentMapper[Velocity] = _
  @Mapper 
  var movingMapper : ComponentMapper[Movable] = _
  @Mapper 
  var roomMapper : ComponentMapper[Room] = _
  
  	  //this will only move characters who have received a movement key and the current component is still set to True
      override def process(e: Entity) {
      	var movable : Movable =  movingMapper.get(e) 
      	var position : Position = positionMapper.get(e)
      	var velocity : Velocity = velocityMapper.get(e)
        var room : Room = roomMapper.get(e)
      	//if moving then process for the given direction
      	if(movable.moving) {
      		var direction : MoveDirection = movable.direction
      		var movement : MovementAction = new MovementAction(direction)
      		movement.process(e)
      	}
        //now check to see if movement has created gone past the map (if so put it at edge)
        val roomEntity : Entity = roomHash(room.id) 
        roomEntity.getComponent(classOf[TileMap]).isPositionInBounds(position)

        
      }

                //p.x += velocity.speed*world.delta
        //p.y += velocity.speed*world.delta
}