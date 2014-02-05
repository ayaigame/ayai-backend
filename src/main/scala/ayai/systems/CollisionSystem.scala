package ayai.systems

/** 
 * ayai.system.CollisionSystem
 */

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.collisions._

/** Artemis Imports **/
import com.artemis.{Aspect, ComponentManager, ComponentMapper, Entity, EntitySystem, World}
import com.artemis.annotations.Mapper
import com.artemis.managers.GroupManager
import com.artemis.utils.{Bag, ImmutableBag, Utils}

import scala.collection.mutable.ArrayBuffer

import scala.collection.mutable.HashMap

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
      //are the two characters within the same x position (is A between B's leftside and width length) 
      val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                              valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

      //are the two characters within the same height area.
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
      //are the two characters within the same x position (is A between B's leftside and width length) 
      val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                              valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

      //are the two characters within the same height area.
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
    //for each room get entities
    val rooms : ImmutableBag[Entity] = world.getManager(classOf[GroupManager]).getEntities("ROOMS")
    val rSize = rooms.size
    for(i <- 0 until rSize) {
      val e = rooms.get(i)
      val tileMap = e.getComponent(classOf[TileMap])
      var quadTree : QuadTree = new QuadTree(0, new Rectangle(0,0,tileMap.getMaximumWidth, tileMap.getMaximumHeight))
      val entities : ImmutableBag[Entity] = world.getManager(classOf[GroupManager]).getEntities("ROOM"+e.getComponent(classOf[Room]).id)
      val eSize = entities.size()
      for(j <- 0 until eSize) {
        quadTree.insert(entities.get(j))
      }

      for(j <- 0 until eSize) {
        var returnObjects : ArrayBuffer[Entity] = new ArrayBuffer[Entity]()
        quadTree.retrieve(returnObjects, entities.get(j))
        for(r <- returnObjects) {
          if(r != entities.get(j)) {
            handleCollision(entities.get(j), r)
          }
        }
      }
    }
  }
}
