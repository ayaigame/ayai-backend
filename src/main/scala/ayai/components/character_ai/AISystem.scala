package ayai.components

import com.artemis.Component
import com.artemis.Entity

class AISystem(world : World) {

	def getScore(current : Position, goal : Position) = Int {
		val dx = abs(current.x - goal.x)
		val dy = abs(current.y - goal.y)
		val dist = dx + dy
		return dist
	}

	def sortByScore(positions : List[Position], goal : Position) = List[Position] {
		val scores = List[Int]()
		for(position <- positions) {
			scores.+(getScore(position, goal))
		}
		return scores.sortWith(_ < _)
	}

	def Astar(start : Position, target : Position) = List[Position] {
		val closed = List[Position]()
		val open = List[Position]()
		val cameFrom = List[Position]()

		open.+(start)
		while(!open.isEmpty){
			val current = open[0]
			cameFrom.+(current)
			if(current == target){
				cameFrom.+(target)
				return cameFrom
			}
			// remove current from open set
			open = open.tail
			// add current to closed set
			closed.+(current)

			val neighbors = findMoves(current)
			val score = getScore(current, target)
			for( neighbor <- neighbors) {
				val repeat = false 
				val alreadyQueued = false
				val tempScore = getScore(neighbor, target)
				if(closed.contains(neighbor)){
					repeat = true 
				}
				if(repeat && tempScore >= score){
					// do nothing
				}
				else{
					if(open.contains(neighbor)){
						alreadyQueued = true 
					}
					if(!alreadyQueued || tempScore < score){
						score = tempScore
						if(!alreadyQueued){
							open.+(neighbor)
							open = sortByScore(open, target)
						}
					}
				}
			}
		}
		// if it makes it here, there is no path to target
	}

	def findMoves(start : Position) = List[Position] {
		// Checks all adjacent positions for walkability and returns list of possible new positions
	}

	def findTarget(start : Position, range : Int) = {
		// Checks if there are any possible targets in range and, if so, returns closest target. Else returns null
	}
}