package ayai.components

import com.artemis.Component

class Stats(stats: List[Stat]) extends Component{



  override def toString: String = {
    "{\"name\": " + name + "}"
  }

  
}