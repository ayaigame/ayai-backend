package com.ayai.main

import com.ayai.main.systems._
import com.artemis.World
import com.artemis.Entity
import com.artemis.managers.GroupManager
import com.ayai.main.components.Player
import com.artemis.ComponentType
import java.lang.Boolean
import com.ayai.main.components.Position

object TestMain  {
  
  var running : Boolean = _
  
  def main(args: Array[String]) {
      println("compiled")
      running = true
      var world: World = new World()
      world.setManager(new GroupManager())
      world.setSystem(new MovementSystem())
      world.initialize()
      world.addEntity(EntityFactory.createPlayer(world, 200, 200))
      
      
      while(running) {
        world.setDelta(5)
        world.process()
        
        render(world)
      }
  }
  
  def render(world : World) {
    Thread.sleep(1000)
    println(world.getEntity(0).getComponent(classOf[Position]).x)
  }
}