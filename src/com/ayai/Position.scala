package com.ayai

import com.artemis.Component

class Position extends Component{
  private var x: Int = _
  private var y : Int = _

  def Position(x: Int, y: Int) {
    this.x = x
    this.y = y
  }
  
  def getXPosition(): Int = {
    return x
  }
  
  def getYPosition(): Int = {
    return y
  }
  
  def setYPosition(y: Int) {
    this.y = y
  }
  
  def setXPosition(x: Int) {
    this.x = x
  }
}