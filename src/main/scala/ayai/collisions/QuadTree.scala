package ayai.collisions

/** Ayai Imports **/
import ayai.components.{Position, Bounds}

/** Crane Imports **/
import crane.Entity

/** External Imports **/
import scala.collection.mutable.ArrayBuffer


/**
** QuadTree Implementation
**/
class QuadTree(var level: Int, var bounds: Rectangle) {
  private val MAX_OBJECTS: Int = 10 
  private val MAX_LEVELS: Int = 8

  private val objects: ArrayBuffer[Entity] = new ArrayBuffer[Entity]()
  private val nodes: Array[QuadTree]  = new Array[QuadTree](4)

  def clear(): Unit = {
    objects.clear()
    nodes.foreach(_.clear())
  }

  // Splits node into 4 subnodes (create 4 leaves on current node)
  private def split(): Unit = {
    val subWidth = bounds.width / 2
    val subHeight = bounds.height / 2
    val x = bounds.x
    val y = bounds.y

    nodes(0) = new QuadTree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight))
    nodes(1) = new QuadTree(level + 1, new Rectangle(x, y, subWidth, subHeight))
    nodes(2) = new QuadTree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight))
    nodes(3) = new QuadTree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight))
  }

  /**
  ** Return the index (the area/ quad section) of the given entity
  **/
  private def getIndex(e: Entity): Int = {
    var index  = -1

    (e.getComponent(classOf[Position]), e.getComponent(classOf[Bounds])) match {
      case(Some(p: Position), Some(bound: Bounds)) => {
        val verticalMidpoint: Double = bounds.x + (bounds.width / 2)
        val horizontalMidpoint: Double = bounds.y + (bounds.height / 2)

        val topQuadrant = (p.y < horizontalMidpoint) && (p.y + bound.height < horizontalMidpoint)
        val bottomQuadrant = p.y > horizontalMidpoint

        // Object can completely fit within the left quadrants
        if ((p.x < verticalMidpoint) && (p.x + bound.width < verticalMidpoint)) {
          if (topQuadrant) {
            index = 1
          }
          else if (bottomQuadrant) {
            index = 2
          }
        } else if (p.x > verticalMidpoint) { // Object can completely fit within the right quadrants
          if (topQuadrant) {
            index = 0
          }
          else if (bottomQuadrant) {
            index = 3
          }
        }

        index
      }
      case _ => -1
    }
  }


  /*
   * Insert the object into the quadtree. If the node
   * exceeds the capacity, it will split and add all
   * objects to their corresponding nodes.
   */
  def insert(e: Entity) {
    if (Option(nodes.head).isDefined) {
      val index = getIndex(e)
      if (getIndex(e) != -1) {
        nodes(index).insert(e)
        return
      }
    }
  
    objects += e
  
    if (objects.size > MAX_OBJECTS && level < MAX_LEVELS) {
      if (nodes.headOption.isEmpty) {
        split()
      }

      var i = 0
      while (i < objects.size) {
        val index = getIndex(objects(i))
        if (index != -1) {
          nodes(index).insert(objects.remove(i))
        } else {
          i += 1
        }
      }
    }
  }

  /*
  * Return all objects that could collide with the given object
  */
  def retrieve(e: Entity): ArrayBuffer[Entity] = {
    val returnObjects = ArrayBuffer.empty[Entity]
    val index = getIndex(e)

    if (index != -1 && nodes.headOption.isDefined) {
      returnObjects ++= nodes(index).retrieve(e)
    }

    objects.foreach(returnObjects += _)

    returnObjects
  }

  /** Return all quadrants **/
  def quadrants: ArrayBuffer[ArrayBuffer[Entity]] = {
    var returnObjects = new ArrayBuffer[ArrayBuffer[Entity]]

    returnObjects += objects

    nodes.flatMap(Option.apply(_)).foreach(returnObjects ++= _.quadrants)

    returnObjects
  }

}
