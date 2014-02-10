package ayai.systems

import com.artemis.{Aspect, ComponentManager, ComponentMapper, Entity, World}
import ayai.components._

class HealthSystem(a : Aspect = Aspect.getAspectForAll(classOf[Health], classOf[Character]).exclude(classOf[Respawn])) extends EntityProcessingSystem(a) {    

  @Mapper
  var characterMapper : ComponentMapper[Character] = _
  @Mapper
  var healthMapper : ComponentMapper[Health] = _

  override def process(e : Entity) {
  	

  }
}