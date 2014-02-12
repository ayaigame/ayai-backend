package ayai.components

import crane.Component
import ayai.actions.Action


class State(var action : Action) extends Component {


	def asJson() {
		("state" -> 
			("action" -> action))
	}
} 