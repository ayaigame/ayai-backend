package com.ayai.main.components

import com.artemis.Component

class Health extends Component{
  private var currentHealth : Int = _
  private var maximumHealth : Int = _
  
  def Health(maximumHealth : Int) {
    this.currentHealth = maximumHealth
    this.maximumHealth = maximumHealth
  }
  
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
}