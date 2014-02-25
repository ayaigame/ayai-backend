
object WorldFactory(networkSystem: ActorSystem) {
  def createWorld(roomName: String): World = {
    var world: World = World() 
    world.addSystem(MovementSystem())
    world.addSystem(RoomChangingSystem())
    world.addSystem(HealthSystem())
    world.addSystem(RespawningSystem())
    world.addSystem(FrameExpirationSystem())
    world.addSystem(NetworkingSystem(networkSystem))
    world.addSystem(CollisionSystem(networkSystem))
    world
  }

}
