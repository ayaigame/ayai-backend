package ayai.apps

import com.artemis.World
import com.artemis.Entity
import com.artemis.managers.GroupManager
import ayai.systems._
import ayai.gamestate._
import ayai.data._
import ayai.actions._
import ayai.components._

object MovementApp {

	def main(args : Array[String]) {
    	var world: World = new World()
    	val running : Boolean = true
    	world.setManager(new GroupManager())
    	world.initialize()

	    var firstRoom: Room = GameState.createRoom(Data.map)
	    val startingRoom = firstRoom.getRoomId
	    var firstPlayer = EntityFactory.createPlayer(world, startingRoom, 2, 2)
	    world.addEntity(firstPlayer)
	    var testItem = EntityFactory.createItem(world,1,3,"ItemTest")
	    world.addEntity(testItem)


	    while(running) {
	      world.setDelta(1)
	      world.process()

	      var ok = true
	      val ln = readLine()
	      println("Before : " + firstPlayer.getComponent(classOf[Position]).x + " " + firstPlayer.getComponent(classOf[Position]).y )
	      if(ln == "a") {
	      	var movement : MovementAction = new MovementAction(new LeftDirection())
	      	movement.process(firstPlayer)
	      } else if (ln == "d") {
	      	var movement : MovementAction = new MovementAction(new RightDirection())
	      	movement.process(firstPlayer)
	      } else if(ln == "s") {
	      	var movement : MovementAction = new MovementAction(new DownDirection())
	      	movement.process(firstPlayer)
	      } else if (ln == "w") {
	      	var movement : MovementAction = new MovementAction(new UpDirection())
	      	movement.process(firstPlayer)
	      }

          println("After: " + firstPlayer.getComponent(classOf[Position]).x + " " + firstPlayer.getComponent(classOf[Position]).y )
	    }
	}
}