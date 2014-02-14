package ayai.systems

import crane.{Entity,EntityProcessingSystem}

import java.util.Map

import ayai.actions._
import ayai.components._
import ayai.systems.RoomChangingSystem

import scala.collection.mutable.HashMap

class MovementSystem(roomHash : HashMap[Long, Entity]) extends EntityProcessingSystem(include=List(classOf[Position], classOf[Velocity],classOf[Room], classOf[Character]), exclude=List(classOf[Transport], classOf[Respawn])) {    
  	  //this will only move characters who have received a movement key and the current component is still set to True
      override def processEntity(e: Entity, delta : Int) {
      	(e.getComponent(classOf[Movable]),
        	e.getComponent(classOf[Position]),
        	e.getComponent(classOf[Velocity]),
          e.getComponent(classOf[Room])) match {
            case (Some(movable : Movable), Some(position : Position), Some(velocity : Velocity), Some(room : Room)) =>
              val originalPosition = new Position(position.x, position.y)
              //if moving then process for the given direction
              if(movable.moving) {
                var direction : MoveDirection = movable.direction
                var movement : MovementAction = new MovementAction(direction)
                movement.process(e)
              }
              
              //now check to see if movement has created gone past the map (if so put it at edge)
              val roomEntity : Entity = roomHash(room.id) 
              //will update position in function
              val tileMap : TileMap = (roomEntity.getComponent(classOf[TileMap])) match {
                case(Some(tileMap : TileMap)) => tileMap
              }
              tileMap.isPositionInBounds(position)
              //if on tile Collision go back to original position
              val collision = tileMap.onTileCollision(position)
              //get room and check if player should change rooms
              //add transport to players (roomchanging system will take over)
              if(collision) {
                position.x = originalPosition.x
                position.y = originalPosition.y
              }
              val transport = tileMap.checkIfTransport(position)
              if(transport != null) {
                e.components += transport
              }
          }
      }
}