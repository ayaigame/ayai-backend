package com.ayai.main.systems

import com.artemis.systems.EntityProcessingSystem
import com.artemis.Aspect
import com.ayai.main.components.Position
import com.artemis.ComponentMapper
import java.util.Map
import com.artemis.Entity
import com.ayai.main.components.Velocity
import com.artemis.annotations.Mapper
import com.ayai.main.maps.GameMap

class MovementSystem(map : GameMap, a: Aspect = Aspect.getAspectFor(classOf[Position], classOf[Velocity])) extends EntityProcessingSystem(a) {    
  @Mapper
  var positionMapper: ComponentMapper[Position] = _
  @Mapper
  var velocityMapper: ComponentMapper[Velocity] = _
  
      override def process(e: Entity) {
        var p: Position = positionMapper.get(e)
        var velocity : Velocity = velocityMapper.get(e)
        var test : Int = world.delta.intValue
        //move position over one
        if(p.x+1 < 0 ||  p.x+1 > map.width || p.y+1 < 0 || p.y+1 > map.height) {
        //if out of bounds dont do anything
          
        }else {
          map.moveEntity(e.getId(),p.x,p.y)
          p.x +=1
          p.y +=1
        }


                //p.x += velocity.speed*world.delta
        //p.y += velocity.speed*world.delta
    }
}