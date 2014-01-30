package ayai.collisions

import com.artemis.Component

import scala.collections.mutable.ArrayBuffer


class QuadTree(val level : Int, val bounds : Rectangle) {
	private val MAX_OBJECTS : Int = 10 
	private val MAX_LEVELS : Int = 5

	private val objects : ArrayBuffer[Entity]()
	private val nodes  = new ArrayBuffer[QuadTree](4)

	def clear() {
		objects.clear

		for(n <- nodes) {
			if(n != null) {
				n.clear
				n = null
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

	private def getIndex(pRect : Rectangle) : Int = {
		val index : Int = -1
		val verticalMidpoint : Double = bounds.getX() + (bounds.getWidth() / 2)
		val horizontalMidpoint : Double = bounds.getY() + (bounds.getHeight() / 2)

		val topQuadrant : Boolean (pRect.getY()) < horizontalMidpoint && pRect.getY() + pRect.getHeight() < horizontalMidpoint)
		val bottomQuadrant : Boolean = (pRect.getY() > horizontalMidpoint)

		   // Object can completely fit within the left quadrants
		if (pRect.getX() < verticalMidpoint && pRect.getX() + pRect.getWidth() < verticalMidpoint) {
	      if (topQuadrant) {
	        index = 1
	      }
	      else if (bottomQuadrant) {
	        index = 2
	      }
	    }
	    // Object can completely fit within the right quadrants
	    else if (pRect.getX() > verticalMidpoint) {
	     if (topQuadrant) {
	       index = 0
	     }
	     else if (bottomQuadrant) {
	       index = 3
	     }
	   }
	 
	   index
	}


	/*
	 * Insert the object into the quadtree. If the node
	 * exceeds the capacity, it will split and add all
	 * objects to their corresponding nodes.
	 */
	def insert(val pRect : Rectangle) {
		if (nodes(0) != null) {
			val index : Int = getIndex(pRect)
			if (index != -1) {
				nodes(index).insert(pRect)
				return
			}
		}
	
		objects.add(pRect);
	
		if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
			if (nodes(0) == null) { 
				split()
			}

			i : Int = 0;
			while (i < objects.size()) {
				val index : Int = getIndex(objects.get(i))
				if (index != -1) {
					nodes(index).insert(objects.remove(i))
				}
				else {
					i++
				}
			}
		}
	}

	/*
	* Return all objects that could collide with the given object
	*/
	def retrieve(val returnObjects : List[Entity], val pRect : Rectangle) {
		index : Int = getIndex(pRect);
		if (index != -1 && nodes(0) != null) {
			nodes(index).retrieve(returnObjects, pRect);
		}

		returnObjects.addAll(objects);

		return returnObjects;
	}	
}