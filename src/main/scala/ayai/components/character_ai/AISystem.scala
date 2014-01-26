package ayai.components

import com.artemis.Component
import com.artemis.Entity

class AISystem(range : Int) {
	val sightRange = range

	def Astar(start : Position, target : Position, map : TileMap) = {
		val closed = List[Position]()
		val open = List[Position]()
		val cameFrom = List[Position]()
		val cost = 0

		open.+(start)
		while(!open.isEmpty){
			var current = open[0]
			if(current == target){
				cameFrom.+(target)
				return cameFrom
			}
			// remove current from open set
			open = open.tail
			// add current to closed set
			closed.+(current)

			var neighbors = findMoves(current)
			for( neighbor <- neighbors) {
				if(closed.contains(neighbor)){
					continue
				}
				
			}
		}
	}

	def findMoves(start : Position) = List[Position] {
		// find all open tiles around start position
	}

	def findTarget(start : Position) = List[Entity] {

	}

	def isVisible(start : Position, target : Position) = {

	}
}