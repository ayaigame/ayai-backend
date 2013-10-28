package com.ayai.main

import com.ayai.main.systems._;
import com.ayai.networking._
import com.artemis.World

import akka.actor._
import akka.actor.Actor._

object TestMain extends App {
  override def main(args: Array[String]) {
      println("compiled")

      val myActor = Actor.actorOf[ConnectionActor].start()
      myActor ! CreateConnection("localhost", 8000)

      var world: World = new World()
      world.setSystem(new MovementSystem())
      world.initialize()
  }
}