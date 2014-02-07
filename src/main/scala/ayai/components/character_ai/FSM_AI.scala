package ayai.components

import com.artemis.Component
import com.artemis.Entity

class FSM_AI() extends AISystem {
	// Simple FSM AI with two states: Patrol (0) or Follow (1)
	val currentState = 0
	val target = null

	def checkState(position : Position, range : int) = {
		// if in Patrol state, checks to see if any targets are in range, and if so switches state
		if(currentState == 0){
			if(findTarget(position, range) != null){
				updateState(1)
			}
		}
	}

	def updateState(newState : int) = {
		currentState = newState
	}
}