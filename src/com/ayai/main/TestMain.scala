package com.ayai.main

import com.ayai.main.systems._;
import com.artemis.World

object TestMain  {
  def main(args: Array[String]) {
      println("compiled")
      var world: World = new World()
      world.setSystem(new MovementSystem())
      world.initialize()
  }
}