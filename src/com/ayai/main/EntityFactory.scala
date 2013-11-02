package com.ayai.main

import com.ayai.main.components.Player
import com.artemis.Entity
import com.artemis.World
import com.ayai.main.components.Position
import com.ayai.main.components.Health
import com.artemis.managers.GroupManager
import com.ayai.main.components.Velocity

object EntityFactory {
  
  /**
   * might have to do some networking stuff, dont know yet
   */
  def createPlayer(world : World, x: Int, y : Int) : Entity = {
    var player : Entity = world.createEntity()
    var position : Position = new Position(3,3)
    var velocity : Velocity = new Velocity(3,4)
    player.addComponent(position)
    
    player.addComponent(velocity)
    
    
    var health : Health = new Health(200,200);
    player.addComponent(health)
    
    player.addComponent(new Player());
    world.getManager(classOf[GroupManager]).add(player, Constants.PLAYER_CHARACTER)
    return player;
  }
}