package com.ayai

import com.ayai.system.MovementSystem
import com.ayai.system.MovementSystem
import com.ayai.system.MovementSystem
import com.artemis.World
import com.ayai.system.MovementSystem

object TestMain {
    def main(args: Array[String]) {
      println("compiled")
      var world: World = new World()
      world.setSystem(new MovementSystem())
      world.initialize()
    }
}