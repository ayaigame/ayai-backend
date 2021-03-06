package ayai.collisions

/** Ayai Imports **/
import ayai.components.{Position, Bounds}

/** Crane Imports **/
import crane.{Component, Entity}

/** External Imports **/
import scala.collection.mutable.ArrayBuffer


/**
** QuadTree Implementation
**/
class QuadTree(var level: Int, var bounds: Rectangle) {
  private val MAX_OBJECTS: Int = 10 
  private val MAX_LEVELS: Int = 8

  private val objects: ArrayBuffer[Entity] = new ArrayBuffer[Entity]()
  private var nodes: Array[QuadTree]  = new Array[QuadTree](4)

  def clear() {
    objects.clear
    for(n <- nodes) {
      if(n != null) {
        n.clear
      }
    }
  }


  // Splits node into 4 subnodes (create 4 leaves on current node)
  private def split() {
    val subWidth: Int = bounds.width / 2
    val subHeight: Int = bounds.height / 2
    val x: Int = bounds.x
    val y: Int = bounds.y

    nodes(0) = new QuadTree(level+1, new Rectangle(x + subWidth, y, subWidth, subHeight))
    nodes(1) = new QuadTree(level+1, new Rectangle(x, y, subWidth, subHeight))
    nodes(2) = new QuadTree(level+1, new Rectangle(x, y + subHeight, subWidth, subHeight))
    nodes(3) = new QuadTree(level+1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight))

  }

  /**
  ** Return the index (the area/ quad section) of the given entity
  **/
  private def getIndex(e: Entity): Int = {
    var index : Int = -1
    (e.getComponent(classOf[Position]), e.getComponent(classOf[Bounds])) match {
    case(Some(p: Position), Some(bound: Bounds)) =>
      val verticalMidpoint: Double = bounds.x + (bounds.width / 2)
      val horizontalMidpoint: Double = bounds.y + (bounds.height / 2)

      val topQuadrant: Boolean = ((p.y < horizontalMidpoint) && (p.y + bound.height < horizontalMidpoint))
      val bottomQuadrant: Boolean = (p.y > horizontalMidpoint)

      // Object can completely fit within the left quadrants
      if ((p.x < verticalMidpoint) && (p.x +  bound.width < verticalMidpoint)) {
        if (topQuadrant) {
          index = 1
        }
        else if (bottomQuadrant) {
          index = 2
        }
      }
      // Object can completely fit within the right quadrants
      else if (p.x > verticalMidpoint) {
       if (topQuadrant) {
         index = 0
       }
       else if (bottomQuadrant) {
         index = 3
       }
      }
     
       index
    case _ => -1
    }
  }


  /*
   * Insert the object into the quadtree. If the node
   * exceeds the capacity, it will split and add all
   * objects to their corresponding nodes.
   */
  def insert(e: Entity) {
    if (nodes(0) != null) {
      var index: Int = getIndex(e)
      if (index != -1) {
        nodes(index).insert(e)
        return
      }
    }
  
    objects += e
  
    if (objects.size > MAX_OBJECTS && level < MAX_LEVELS) {
      if (nodes(0) == null) { 
        split()
      }

      var i: Int = 0
      while (i < objects.size) {
        val index: Int = getIndex(objects(i))
        if (index != -1) {
          nodes(index).insert(objects.remove(i))
        }
        else {
          i = i+1
        }
      }
    }
  }

  /*
  * Return all objects that could collide with the given object
  */
  def retrieve(e: Entity): ArrayBuffer[Entity] = {
    var returnObjects: ArrayBuffer[Entity] = ArrayBuffer.empty[Entity]
    var index: Int = getIndex(e)
    if (index != -1 && nodes(0) != null) {
      returnObjects = nodes(index).retrieve(e)
    }

    for(r <- objects) {
      returnObjects += r
    }
    returnObjects
  }

  /** Return all quadrants **/
  def quadrants: ArrayBuffer[ArrayBuffer[Entity]] = {
    var returnObjects = new ArrayBuffer[ArrayBuffer[Entity]]
  
    if (objects.length > 0) 
    returnObjects += objects

    for(node <- nodes) {
      if(node != null) {
      returnObjects ++= node.quadrants
      }
    }
    return returnObjects
  }

}
