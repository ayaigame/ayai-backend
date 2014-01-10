package ayai.apps

import ayai.gamestate.GameState
import ayai.components.Player
import com.artemis.Entity
import com.artemis.World
import ayai.components.Position
import ayai.components.Health
import com.artemis.managers.GroupManager
import ayai.components._
import com.artemis.Component
import ayai.components.Containable

object EntityFactory {
  
  /**
   * might have to do some networking stuff, dont know yet
   */
  def createPlayer(world : World, roomId: Int, x: Int, y : Int) : Entity = {
    var player : Entity = world.createEntity()
    var position : Position = new Position(x,y)
    var velocity : Velocity = new Velocity(3,4)
    player.addComponent(position)
    
    player.addComponent(velocity)
    
    
    var health : Health = new Health(200,200);
    player.addComponent(health)
    
    player.addComponent(new Player(GameState.getNextPlayerId()));
    GameState.addPlayer(roomId, player)
    world.getManager(classOf[GroupManager]).add(player, Constants.PLAYER_CHARACTER)
    println("Entity: " + player.getId())
    player;
  }

  def createItem(world : World, x : Int, y :Int, name : String) : Entity = {
    var item : Entity = world.createEntity()
    var position : Position = new Position(x, y)
    item.addComponent(position)

    var containable : Containable = new Containable(3, name, null)
    item.addComponent(containable)

    world.getManager(classOf[GroupManager]).add(item,"ITEM")
    
    item
  }

  def createRoom(world : World, id : Int) : Entity = {
    var room : Entity = world.createEntity()
    var tileMap : TileMap = new TileMap()
    var roomId : Room = new Room(id)

    room.addComponent(tileMap)
    room.addComponent(roomId)

    world.getManager(classOf[GroupManager]).add(room,"ROOMS")

    room

  }
}
