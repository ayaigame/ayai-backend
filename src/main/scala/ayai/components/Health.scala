package ayai.components

import com.artemis.Component

class Health(var currentHealth: Int, var maximumHealth: Int) extends Component{
  
  def getCurrentHealth() : Int = {
    return this.currentHealth
  }
  
  def setCurrentHealth(playerHealth : Int){
    this.currentHealth = playerHealth
  }
  
  def getMaximumHealth() : Int = {
    return this.maximumHealth
  }
  
  def setMaximumHealth(maximumHealth : Int) {
    this.maximumHealth = maximumHealth
  }
  
  def isAlive : Boolean = {
    return currentHealth > 0
  }
  
  def addDamage(damage:Float) {
    currentHealth.-(damage) 
    if(currentHealth < 0) {
      currentHealth = 0
    }
  }

  override def toString: String = {
    "{\"current\": " + currentHealth + ", \"max\": " + maximumHealth + "}"
  }
}