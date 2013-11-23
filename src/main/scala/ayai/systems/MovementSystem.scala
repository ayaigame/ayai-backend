package ayai.systems

import com.artemis.systems.EntityProcessingSystem
import com.artemis.Aspect
import ayai.components.Position
import com.artemis.ComponentMapper
import java.util.Map
import com.artemis.Entity
import ayai.components.Velocity
import com.artemis.annotations.Mapper
import ayai.maps.GameMap

class MovementSystem(map : GameMap, a: Aspect = Aspect.getAspectFor(classOf[Position], classOf[Velocity])) extends EntityProcessingSystem(a) {    
  @Mapper
  var positionMapper: ComponentMapper[Position] = _
  @Mapper
  var velocityMapper: ComponentMapper[Velocity] = _
  
      override def process(e: Entity) {
        }

                //p.x += velocity.speed*world.delta
        //p.y += velocity.speed*world.delta
    }
}