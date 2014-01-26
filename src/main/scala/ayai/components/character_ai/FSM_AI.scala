package ayai.components

import com.artemis.Component
import com.artemis.Entity

class FSM_AI(val states : List[State]) extends AISystem {
	// assumes starting state is states[0]
	val currentState = states[0]

	def checkState() = {
		val tempCondition = currentState.condition
		if(tempCondition.checkCondition){
			updateState(currentState.nextState)
		}
	}

	def updateState(newState : State) = {
		currentState = newState
	}
}