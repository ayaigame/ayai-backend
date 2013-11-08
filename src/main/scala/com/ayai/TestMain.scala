package com.ayai.main

import com.ayai.main.systems._
import com.ayai.main.gamestate._
import com.artemis.World
import com.artemis.Entity
import com.artemis.managers.GroupManager
import com.ayai.main.components.Player
import com.ayai.main.networking._
import com.artemis.ComponentType
import java.lang.Boolean
import com.ayai.main.components.Position
import com.ayai.main.maps.GameMap
import com.ayai.main.data._
import scala.util.parsing.json.JSONObject

object TestMain  {
def arrayToString(a: Array[Array[Int]]) : String = {
       val str = for (l <- a) yield l.mkString("[", ",", "]")
       str.mkString("[",",\n","]")
    }
  var running : Boolean = _
  var map : GameMap = new GameMap(10,10)
  def main(args: Array[String]) {
    println("compiled")
    running = true
    var world: World = new World()
    world.setManager(new GroupManager())
    world.setSystem(new MovementSystem(map))
    world.initialize()

    println(arrayToString(map.map))
    var firstRoom: Room = GameState.createRoom(arrayToString(map.map))
    val startingRoom = firstRoom.getRoomId
    world.addEntity(EntityFactory.createPlayer(world, startingRoom, 2, 2))
    world.addEntity(EntityFactory.createItem(world,1,3,"ItemTest"))    
    var receptionist = new Receptionist(8007)
    receptionist.start()

    while(running) {
      world.setDelta(1)
      world.process()
      
      render(world)
    }
  }
  
  def render(world : World) {
    // sleep for one second (dont want to process too much now)
    Thread.sleep(10000)
    //print out map
    println("New Map")
    var r : Int = 0
    var h = 0
    for(r <- 0 to map.width-1) {
      for (h <- 0 to map.height-1) {
        if(map.isOccupied(r,h)) {
          print(map.getEntityId(r,h))
        } else {
          print(map.map(r)(h))
        }
      }
      println()
    }
    //println(world.getEntity(0).getComponent(classOf[Position]).x)
  }
}
