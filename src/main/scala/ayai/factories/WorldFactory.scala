package ayai.factories

import ayai.apps.Constants
import ayai.systems._
import ayai.gamestate.{RoomWorld, GameStateSerializer, MessageProcessorSupervisor, TileMap}
import akka.actor.{ActorSystem, Props}

object WorldFactory {
  def apply(networkSystem: ActorSystem) = new WorldFactory(networkSystem)
}

class WorldFactory(networkSystem: ActorSystem) {
  /**
  ** Create a world and instantiate all needed systems and create message processors
  **/
  def createWorld(name: String, file: String): RoomWorld = {
    val tileMap = EntityFactory.loadRoomFromJson(s"$file.json")
    var world: RoomWorld = RoomWorld(name, tileMap)

    world.addSystem(MovementSystem(), 1)
    world.addSystem(TransportSystem(networkSystem), 2)
    world.addSystem(HealthSystem())
    world.addSystem(RespawningSystem())
    world.addSystem(FrameExpirationSystem())
    world.addSystem(StatusEffectSystem())
    world.addSystem(ItemSystem())
    world.addSystem(StatusEffectSystem())
    world.addSystem(LevelingSystem(networkSystem))
    // world.addSystem(DirectorSystem())
    world.addSystem(NPCRespawningSystem())
    world.addSystem(GoalSystem(networkSystem))
    world.addSystem(RoomChangingSystem(networkSystem))
    world.addSystem(NetworkingSystem(networkSystem), 3)
    world.addSystem(CollisionSystem(networkSystem))
    world.addSystem(AttackSystem(networkSystem))
    world.addSystem(CooldownSystem(networkSystem))
    val serializer = networkSystem.actorOf(Props(GameStateSerializer(world)), s"Serializer$name")
    val nmProcessor = networkSystem.actorOf(Props(MessageProcessorSupervisor(world)), name=s"MProcessor$name")

    //HARDCODED ENTITY ADD HAPPENS HERE
    val entity = EntityFactory.createAI(world, "axis")
    world.addEntity(entity)

    world
  }
}
