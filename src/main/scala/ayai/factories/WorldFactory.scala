package ayai.factories

import ayai.apps.Constants
import ayai.systems._
import ayai.gamestate.{RoomWorld, GameStateSerializer, MessageProcessorSupervisor, TileMap}
import akka.actor.{ActorSystem, Props}
object WorldFactory {
  def apply(networkSystem: ActorSystem) = new WorldFactory(networkSystem)
}

class WorldFactory(networkSystem: ActorSystem) {
  def createWorld(name: String, file: String): RoomWorld = {
    val tileMap = EntityFactory.loadRoomFromJson(s"$file.json")
    var world: RoomWorld = RoomWorld(name, tileMap)
    world.addSystem(MovementSystem())
    world.addSystem(RoomChangingSystem())
    world.addSystem(HealthSystem())
    world.addSystem(RespawningSystem())
    world.addSystem(FrameExpirationSystem())
    world.addSystem(NetworkingSystem(networkSystem))
    world.addSystem(CollisionSystem(networkSystem))
    val serializer = networkSystem.actorOf(Props(GameStateSerializer(world)), s"Serializer$name")
    val nmProcessor = networkSystem.actorOf(Props(MessageProcessorSupervisor(world)), name="MProcessor$name")

    ItemFactory.bootup(world)
    ClassFactory.bootup(world)
    world
  }
}
