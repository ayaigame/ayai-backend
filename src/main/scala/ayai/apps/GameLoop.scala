package ayai.apps

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.rmi.server.UID

import ayai.systems._
import ayai.gamestate._
import com.artemis.World
import com.artemis.Entity
import com.artemis.managers.GroupManager
import ayai.components.Player
import ayai.networking._
import com.artemis.ComponentType
import java.lang.Boolean
import ayai.components.Position
import ayai.maps.GameMap
import ayai.data._
import scala.util.parsing.json.JSONObject

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


object GameLoop {
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
    var firstPlayer = EntityFactory.createPlayer(world, startingRoom, 2, 2)
    world.addEntity(firstPlayer)
    var testItem = EntityFactory.createItem(world,1,3,"ItemTest")
    world.addEntity(testItem)

    implicit val timeout = Timeout(5 seconds)
    val networkSystem = ActorSystem("NetworkSystem")
    val messageQueue = networkSystem.actorOf(Props(new NetworkMessageQueue()), name = (new UID()).toString)
    val interpreter = networkSystem.actorOf(Props(new NetworkMessageInterpreter(messageQueue)), name = (new UID()).toString)
    // val connectionManager = networkSystem.actorOf(Props(new ConnectionManager(networkSystem, interpreter)), name = (new UID()).toString)
    val messageProcessor = networkSystem.actorOf(Props(new NetworkMessageProcessor(networkSystem)), name = (new UID()).toString)
    // val receptionist = new Receptionist(8007, connectionManager)
    val receptionist = new SockoServer(networkSystem, interpreter)
    receptionist.run(8007)

    //This is to demonstrate how to get the Ids for the GroupManager
    // println("!!!!!!!!!!!!!")
    // println(firstPlayer.getId())
    // println(testItem.getId())

    while(running) {
      world.setDelta(1)
      world.process()

      val future = messageQueue ? new FlushMessages() // enabled by the “ask” import
      val result = Await.result(future, timeout.duration).asInstanceOf[QueuedMessages]

      result.messages.foreach { message =>
        messageProcessor ! new ProcessMessage(message)
      }
      // Thread.sleep(1000)
      // render(world)
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
