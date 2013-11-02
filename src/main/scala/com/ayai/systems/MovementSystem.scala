package com.ayai.main.systems

import com.artemis.systems.EntityProcessingSystem
import com.artemis.Aspect
import com.ayai.main.components.Position
import com.artemis.ComponentMapper
import java.util.Map
import com.artemis.Entity
import com.ayai.main.components.Velocity
import com.artemis.annotations.Mapper

class MovementSystem(a: Aspect = Aspect.getAspectFor(classOf[Position]) ) extends EntityProcessingSystem(a) {    
  @Mapper
  var positionMapper: ComponentMapper[Position] = _
  @Mapper
  var velocityMapper: ComponentMapper[Velocity] = _
  
      override def process(e: Entity) {
        var p: Position = positionMapper.get(e)
        var velocity : Velocity = velocityMapper.get(e)
        var test : Int = world.delta.intValue()
        p.x += velocity.speed*world.delta
        p.y += velocity.speed*world.delta
    }
}