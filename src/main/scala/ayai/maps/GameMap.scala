// package ayai.maps

// import ayai.components.Position
// import scala.collection.mutable.Map
// class GameMap(var width : Int, var height : Int) {
// 	//this is a map of the normal values (or terrain values if needed)
// 	var map : Array[Array[Int]] = Array.fill[Int](width,height)(0)
	
// 	//this is a list of
// 	private var entityByCoord : Array[Array[Int]] = Array.fill[Int](width,height)(-1)
// 	private var coordByEnitity : Map[Int, Position] = Map()
	

// 	//add entity to map
// 	def addEntity(id : Int, x : Int,  y : Int) {
// 		entityByCoord(x)(y) = id
// 		coordByEnitity.put(id,new Position(x,y))
// 	}
// 	//remove entity
// 	def removeEntity(id : Int) {
// 		var position : Position = coordByEnitity(id)
// 		entityByCoord(position.x)(position.y) = -1
// 		coordByEnitity.remove(id)
// 	}

// 	//move entity to new location
// 	def moveEntity(id : Int, x : Int, y : Int) {
// 		var position : Position = new Position(x,y)
// 		var oldPosition : Position = getPosition(id)
// 		coordByEnitity.remove(id)
// 		coordByEnitity.put(id,position)
// 		entityByCoord(oldPosition.x)(oldPosition.y) = -1
// 		entityByCoord(position.x)(position.y) = id
// 	}

// 	//get position of entity
// 	def getPosition(id : Int) : Position = {
// 		coordByEnitity(id)
// 	}

// 	//return if an entity is in this location
// 	def isOccupied(x : Int, y : Int) : Boolean = {
// 		entityByCoord(x)(y) > -1
// 	}

// 	def getEntityId(x : Int, y : Int) : Int = {
// 		entityByCoord(x)(y)
// 	}

// 	def getJSONMap() : String =  {
// 		""
// 	}

// }