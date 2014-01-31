package ayai.components

import com.artemis.Component


class Class(id: Int, name: String, baseHealth: Int, baseMana: Int) extends Component{



  override def toString: String = {
    "{\"name\": " + name + "}"
  }

  
}