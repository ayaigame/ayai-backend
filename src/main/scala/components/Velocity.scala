package com.ayai.main.components

import com.artemis.Component

class Velocity extends Component {
  private var angle : Int = _
  private var speed : Int = _
  
  def Velocity(angle: Int, speed: Int) {
    this.angle = angle
    this.speed = speed
  }
  
  def getAngle() : Int = {
    return angle
  }
  
  def setAngle(angle: Int) {
    this.angle = angle
  }
  
  def getSpeed() : Int = {
    return speed
  }
  
  def setSpeed(speed :Int ) {
    this.speed = speed
  }
  
}