package ayai.components

import com.artemis.Component
import com.artemis.Entity

class FSM_AI() extends AISystem {
	val currentState = 0
	val target = null

	def checkState(position : something, sight : int) = {
		if(currentState == 0){
			tempObjects = findObjects(position, sight)
			if(tempObjects){
				target = tempObjects[0]
				updateState(1)
			}
		}
		if(currentState == 1){
			Astar(origin, target, map)
		}
	}

	def updateState(newState : int) = {
		currentState = newState
	}
}