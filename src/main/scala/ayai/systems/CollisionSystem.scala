package ayai.systems

/** 
 * ayai.system.CollisionSystem
 */

/** Ayai Imports **/
import ayai.components.{Position, Bounds}

/** Artemis Imports **/
import com.artemis.{Aspect, ComponentManager, ComponentMapper, Entity, EntitySystem, World}
import com.artemis.annotations.Mapper
import com.artemis.managers.GroupManager
import com.artemis.utils.{Bag, ImmutableBag, Utils}

class CollisionSystem(world: World) extends EntitySystem(Aspect.getAspectForAll(classOf[Position], classOf[Bounds])) {
  @Mapper
  val pm: ComponentMapper[Position] = world.getMapper(classOf[Position])
  @Mapper
  val bm: ComponentMapper[Bounds] = world.getMapper(classOf[Bounds])

  def valueInRange(value: Int, min: Int, max: Int): Boolean = {
    return (value >= min) && (value <= max)
  }

  def handleCollision(entityA: Entity, entityB: Entity) {
      val positionA = pm.get(entityA)
      val boundsA = bm.get(entityA)
      val positionB = pm.get(entityB)
      val boundsB = bm.get(entityB)

      // Remember to end a line with an operator of some sort (., +, &&, ||) if you need 
      // to not fall afoul of the automatic end of statement guesser 
      val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                              valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

      val yOverlap: Boolean = valueInRange(positionA.y, positionB.y, positionB.y + boundsB.height) ||
                              valueInRange(positionB.y, positionA.y, positionA.y + boundsA.height)

      if(xOverlap && yOverlap) {
        println("OVERLAP!")
      }

  }

  override def checkProcessing: Boolean =  {
    true
  }
  override def processEntities(entities: ImmutableBag[Entity]) {
    val enemies: ImmutableBag[Entity] = world.getManager(classOf[GroupManager]).getEntities("ENEMIES")
    val players: ImmutableBag[Entity] = world.getManager(classOf[GroupManager]).getEntities("PLAYERS")
    val pSize = players.size
    val eSize = enemies.size
    for( i <- 0 until pSize) {
      for( j <- 0 until eSize) {
        handleCollision(players.get(i), enemies.get(j))
      }
    }
  }
}
