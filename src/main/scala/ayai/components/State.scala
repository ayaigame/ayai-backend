package ayai.components

/** Ayai Imports **/
import ayai.actions.Action

/** Crane Imports **/
import crane.Component


class State(var action : Action) extends Component {
  def asJson() {
    ("state" -> 
      ("action" -> action))
  }
} 
