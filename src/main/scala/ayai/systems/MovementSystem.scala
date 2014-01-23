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
import ayai.components.Character
import ayai.components.TileMap
import ayai.components.Transport

import scala.collection.mutable.HashMap

class MovementSystem(roomHash : HashMap[Int, Entity], a: Aspect = Aspect.getAspectForAll(classOf[Position], classOf[Velocity],classOf[Movable], classOf[Room], classOf[Character])) extends EntityProcessingSystem(a) {    
  @Mapper
  var positionMapper: ComponentMapper[Position] = _
  @Mapper
  var velocityMapper: ComponentMapper[Velocity] = _
  @Mapper 
  var movingMapper : ComponentMapper[Movable] = _
  @Mapper 
  var roomMapper : ComponentMapper[Room] = _
  @Mapper
  var characterMapper : ComponentMapper[Character] = _
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
        //will update position in function
        val tileMap : TileMap = roomEntity.getComponent(classOf[TileMap])
        tileMap.isPositionInBounds(position)

        
        //get room and check if player should change rooms

        //tileMap.checkIfTransport(roomEntity, position)
        //add transport to players (roomchanging system will take over)
      }

                //p.x += velocity.speed*world.delta
        //p.y += velocity.speed*world.delta
}