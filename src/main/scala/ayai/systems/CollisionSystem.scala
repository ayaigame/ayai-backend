package ayai.systems

/** 
 * ayai.system.CollisionSystem
 */

/** Ayai Imports **/
import ayai.actions._
import ayai.components.{Position, Bounds, Bullet, Health}

/** Artemis Imports **/
import com.artemis.{Aspect, ComponentManager, ComponentMapper, Entity, EntitySystem, World}
import com.artemis.annotations.Mapper
import com.artemis.managers.GroupManager
import com.artemis.utils.{Bag, ImmutableBag, Utils}

/** External Imports **/
import scala.math.abs
import scala.util.control.Breaks._

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
      //are the two players within the same x position (is A between B's leftside and width length) 
      val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                              valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

      //are the two players within the same height area.
      val yOverlap: Boolean = valueInRange(positionA.y, positionB.y, positionB.y + boundsB.height) ||
                              valueInRange(positionB.y, positionA.y, positionA.y + boundsA.height)

      if(xOverlap && yOverlap) {
        if(abs(positionA.y - positionB.y) < abs(positionA.x - positionB.x)) {
          if(positionA.x < positionB.x) {
            new MovementAction(new LeftDirection).process(entityA)
            new MovementAction(new RightDirection).process(entityB)
          } else {
            new MovementAction(new RightDirection).process(entityA)
            new MovementAction(new LeftDirection).process(entityB)
          }
        } else {
          if(positionA.y < positionB.y) {
            new MovementAction(new UpDirection).process(entityA)
            new MovementAction(new DownDirection).process(entityB)
          } else {
            new MovementAction(new DownDirection).process(entityA)
            new MovementAction(new UpDirection).process(entityB)
          }
        }
    //    println("OVERLAP!")
      }

  }

  def handleBulletCollision(entityA: Entity, entityB: Entity) {
      val positionA = pm.get(entityA)
      val boundsA = bm.get(entityA)
      val positionB = pm.get(entityB)
      val boundsB = bm.get(entityB)
      // Remember to end a line with an operator of some sort (., +, &&, ||) if you need 
      // to not fall afoul of the automatic end of statement guesser
      //are the two players within the same x position (is A between B's leftside and width length) 
      val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                              valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

      //are the two players within the same height area.
      val yOverlap: Boolean = valueInRange(positionA.y, positionB.y, positionB.y + boundsB.height) ||
                              valueInRange(positionB.y, positionA.y, positionA.y + boundsA.height)

      if(xOverlap && yOverlap) {
        entityA.getComponent(classOf[Health]).currentHealth -= entityB.getComponent(classOf[Bullet]).damage
        world.deleteEntity(entityB)
      }
    //    println("OVERLAP!")
  }

  override def checkProcessing: Boolean =  {
    true
  }
  override def processEntities(entities: ImmutableBag[Entity]) {
    val players: ImmutableBag[Entity] = world.getManager(classOf[GroupManager]).getEntities("PLAYERS")
    val bullets : ImmutableBag[Entity] = world.getManager(classOf[GroupManager]).getEntities("BULLETS")
    val bSize  = bullets.size
    val pSize = players.size
    //do players first
    for( i <- 0 until pSize) {
      for( j <- (i + 1) until pSize) {
          if(i != j) 
            handleCollision(players.get(i), players.get(j))

      }
    }


    for( j <- 0 until bSize) {
      breakable {
        for( i <- 0 until pSize) {
        //could also set victim here, and let other systme (but that stuffs later)
          if(bullets.get(j).getComponent(classOf[Bullet]).initiator != players.get(i)) {
            handleBulletCollision(players.get(i), bullets.get(j))
            //now break out and go to next bullet
            break
          }
        }
      }
    }
  }

}
