package ayai.collisions

import crane.Component
import crane.Entity

import ayai.components.Position
import ayai.components.Bounds

import scala.collection.mutable.ArrayBuffer


class QuadTree(var level : Int, var bounds : Rectangle) {
	private val MAX_OBJECTS : Int = 10 
	private val MAX_LEVELS : Int = 5

	private val objects : ArrayBuffer[Entity] = new ArrayBuffer[Entity]()
	private var nodes : Array[QuadTree]  = new Array[QuadTree](4)

	def clear() {
		objects.clear
		for(n <- nodes) {
			if(n != null) {
				n.clear
			}
		}
	}


	// Splits node into 4 subnodes
	private def split() {
		val subWidth : Int = bounds.getWidth() / 2
		val subHeight : Int = bounds.getHeight() / 2
		val x : Int = bounds.getX()
		val y : Int = bounds.getY()

		nodes(0) = new QuadTree(level+1, new Rectangle(x + subWidth, y, subWidth, subHeight))
		nodes(1) = new QuadTree(level+1, new Rectangle(x, y, subWidth, subHeight))
		nodes(2) = new QuadTree(level+1, new Rectangle(x, y + subHeight, subWidth, subHeight))
		nodes(3) = new QuadTree(level+1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight))

	}

	private def getIndex(e : Entity) : Int = {
		var index : Int = -1
		(e.getComponent(classOf[Position]), e.getComponent(classOf[Bounds])) match {
		case(Some(p: Position), Some(bound: Bounds)) =>
			val verticalMidpoint : Double = bounds.getX() + (bounds.getWidth() / 2)
			val horizontalMidpoint : Double = bounds.getY() + (bounds.getHeight() / 2)

			val topQuadrant : Boolean = ((p.getY() < horizontalMidpoint) && (p.getY() + bound.getHeight() < horizontalMidpoint))
			val bottomQuadrant : Boolean = (p.getY() > horizontalMidpoint)

			   // Object can completely fit within the left quadrants
			if ((p.getX() < verticalMidpoint) && (p.getX() +  bound.getWidth() < verticalMidpoint)) {
		      if (topQuadrant) {
		        index = 1
		      }
		      else if (bottomQuadrant) {
		        index = 2
		      }
		    }
		    // Object can completely fit within the right quadrants
		    else if (p.getX() > verticalMidpoint) {
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
	def insert(e : Entity) {
		if (nodes(0) != null) {
			var index : Int = getIndex(e)
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

			var i : Int = 0
			while (i < objects.size) {
				val index : Int = getIndex(objects(i))
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
	def retrieve(e  : Entity) : ArrayBuffer[Entity] = {
		var returnObjects : ArrayBuffer[Entity] = ArrayBuffer.empty[Entity]
		var index : Int = getIndex(e)
		if (index != -1 && nodes(0) != null) {
			returnObjects = nodes(index).retrieve(e)
		}

		for(r <- objects) {
			returnObjects += r
		}
		returnObjects
	}
}