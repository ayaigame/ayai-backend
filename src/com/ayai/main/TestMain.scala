package com.ayai.main

import com.ayai.main.systems._;
import com.artemis.World

import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration._

object TestMain extends App {
  def main(args: Array[String]) {
      println("compiled")

      val myActor: ActorRef = actorOf[ConnectionActor].start()
      myActor ! (new CreateConnection("localhost", 8000))

      var world: World = new World()
      world.setSystem(new MovementSystem())
      world.initialize()
  }
}