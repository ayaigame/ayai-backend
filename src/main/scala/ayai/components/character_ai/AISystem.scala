package ayai.components

import com.artemis.Component
import com.artemis.Entity

class AISystem(range : Int) {
	val sightRange = range

	def getScore(current : Position, goal : Position) = Int {
		val dx = abs(current.x - goal.x)
		val dy = abs(current.y - goal.y)
		val dist = dx + dy
		return dist
	}

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
/** Sample code that I had working in python
def aStar(state, heuristic):
	closedset = []
	openset = []
	solution = []
	openset.append(copy.deepcopy(state))

	while(len(openset) > 0):
		current = copy.deepcopy(openset[0])

		if(puzzleComplete(current)):
			displayGameState(current)
			break

		openset.pop(0)
		closedset.append(copy.deepcopy(current))
		moves = findAllMoves(current)
		score = getScore(current, heuristic)

		for i in moves:
			repeat = False
			alreadyQueued = False
			testNode = copy.deepcopy(current)
			testNode = applyMove(testNode, i)
			testNode = normalize(testNode)
			tempScore = getScore(testNode, heuristic)

			for j in closedset:
				if(compare(testNode, j)):
					repeat = True
					break

			if(repeat and tempScore >= score):
				continue

			for k in openset:
				if(compare(testNode, k)):
					alreadyQueued = True
					break

			if(not(alreadyQueued) or tempScore < score):
				score = tempScore
				if(not(alreadyQueued)):
					if(len(openset) > 0):
						for x in openset:
							if(tempScore <= getScore(x, heuristic)):
								openset.insert(openset.index(x), copy.deepcopy(testNode))
								break
							if(openset.index(x) == len(openset)-1):
								openset.append(copy.deepcopy(testNode))
					if(len(openset) == 0):
						openset.append(copy.deepcopy(testNode))
**/

	def findMoves(start : Position) = List[Position] {
		// find all open tiles around start position
	}

	def findTarget(start : Position) = List[Entity] {

	}

	def isVisible(start : Position, target : Position) = {

	}
}