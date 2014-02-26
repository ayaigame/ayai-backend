package ayai.factories

import ayai.apps.Constants
import ayai.systems._
import ayai.gamestate.{RoomWorld, GameStateSerializer}
import akka.actor.{ActorSystem, Props}
object WorldFactory {
  def apply(networkSystem: ActorSystem) = new WorldFactory(networkSystem)
}

class WorldFactory(networkSystem: ActorSystem) {
  def createWorld(name: String, file: String): RoomWorld = {
    var world: RoomWorld = RoomWorld(name)
    world.addSystem(MovementSystem())
    world.addSystem(RoomChangingSystem())
    world.addSystem(HealthSystem())
    world.addSystem(RespawningSystem())
    world.addSystem(FrameExpirationSystem())
    world.addSystem(NetworkingSystem(networkSystem))
    world.addSystem(CollisionSystem(networkSystem))
    world.addEntity(EntityFactory.loadRoomFromJson(name.toInt, s"$file.json"))
    val serializer = networkSystem.actorOf(Props(new GameStateSerializer(world, Constants.LOAD_RADIUS)), s"Serializer$name")
    val processor = networkSystem.actorOf(Props(new GameStateSerializer(world, Constants.LOAD_RADIUS)), s"Serializer$name")
    val nmProcessor = networkSystem.actorOf(Props(NetworkMessageProcessorSupervisor(world, socketMap)), name="NMProcessor$name")

    ItemFactory.bootup(world)
    ClassFactory.bootup(world)
    world
  }
}
