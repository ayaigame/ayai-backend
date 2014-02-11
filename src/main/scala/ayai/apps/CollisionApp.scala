// package ayai.apps

// import ayai.components.{Position, Bounds}
// import ayai.systems.CollisionSystem

// import crane.{Entity, World}


// object CollisionApp {
//   def main(args: Array[String]) {
//     val world: World = new World
//     world.setSystem(new CollisionSystem(world))
//     world.setManager(new GroupManager)
//     world.initialize

//     val p: Entity = world.createEntity
//     p.components += (new Position(10, 10))
//     p.components += (new Bounds(10, 10))
//     p.addToWorld
//     world.getManager(classOf[GroupManager]).add(p, "CHARACTERS")

//     // This Should Collide
//     val colE: Entity = world.createEntity
//     colE.components += (new Position(5, 5))
//     colE.components += (new Bounds(10, 10))
//     colE.addToWorld
//     world.getManager(classOf[GroupManager]).add(colE, "ENEMIES")

//     // This Should Not Collide
//     val ncolE: Entity = world.createEntity
//     ncolE.components += (new Position(21, 21))
//     ncolE.components += (new Bounds(10, 10))
//     ncolE.addToWorld
//     world.getManager(classOf[GroupManager]).add(ncolE, "ENEMIES")

//     println("Note: You should see one \"OVERLAP!\" and then \"DONE\"")
//     world.process
//     println("DONE")
//   }
  
// }
