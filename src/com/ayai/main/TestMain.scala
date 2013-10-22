package com.ayai.main

import com.ayai.main.systems._;
import com.artemis.World

object TestMain extends App {
  
      println("compiled")
      var world: World = new World()
      world.setSystem(new MovementSystem())
      world.initialize()
}