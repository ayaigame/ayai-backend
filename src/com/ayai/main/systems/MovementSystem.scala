package com.ayai.main.systems

import com.artemis.systems.EntityProcessingSystem
import com.artemis.Aspect
import com.ayai.main.components.Position
import com.artemis.ComponentMapper
import java.util.Map
import com.artemis.Entity

class MovementSystem(a: Aspect = Aspect.getAspectFor(classOf[Position]) ) extends EntityProcessingSystem(a) {    

    private var positionMapper: ComponentMapper[Position] = _
    
    
    override def initialize() {
      println("initialized")
      positionMapper = world.getMapper(classOf[Position])
    }
  
    override def process(e: Entity) {
        var p: Position = positionMapper.get(e)
        p.setXPosition(0)
        p.setYPosition(0)
    }
}