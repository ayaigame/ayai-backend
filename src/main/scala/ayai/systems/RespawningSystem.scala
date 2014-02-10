package ayai.systems

import com.artemis.{Aspect, ComponentManager, ComponentMapper, Entity, World}
import com.artemis.systems.EntityProcessingSystem

import ayai.components._

class RespawningSystem( a: Aspect = Aspect.getAspectForAll(classOf[Room], classOf[Character], classOf[Respawn])) extends EntityProcessingSystem(a) {    
  @Mapper
  var characterMapper: ComponentMapper[Character] = _
  @Mapper
  var roomMapper: ComponentMapper[Velocity] = _
  @Mapper
  var respawnMapper : ComponentMapper[Respawn] = _

  override def process(e : Entity) {
  	var respawn : Respawn = respawnMapper.get(e)


  	if(respawn.isReady(System.currentTimeMillis))) {
		//if ready to respawn 
		world.deleteEntity(e)
		world.changedEntity(e)
	}
  }
}