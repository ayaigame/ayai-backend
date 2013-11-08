package com.ayai.main

import com.ayai.main.gamestate.GameState
import com.ayai.main.components.Player
import com.artemis.Entity
import com.artemis.World
import com.ayai.main.components.Position
import com.ayai.main.components.Health
import com.artemis.managers.GroupManager
import com.ayai.main.components.Velocity
import com.artemis.Component
import com.ayai.main.components.Containable

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
    TestMain.map.addEntity(player.getId(),x,y)
    player;
  }

  def createItem(world : World, x : Int, y :Int, name : String) : Entity = {
    var item : Entity = world.createEntity()
    var position : Position = new Position(x, y)
    item.addComponent(position)

    var containable : Containable = new Containable(3, name)
    item.addComponent(containable)

    world.getManager(classOf[GroupManager]).add(item,"ITEM")
    TestMain.map.addEntity(item.getId(),x,y)
    item
  }
}