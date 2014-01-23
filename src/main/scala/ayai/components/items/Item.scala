package ayai.components

import com.artemis.Component


class Item(name: String, itemType: String, value: Int, weight: Double) extends Component{



  override def toString: String = {
    "{\"name\": " + name + "}"
  }

  
}