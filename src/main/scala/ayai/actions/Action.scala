package ayai.actions

trait Action {
	//will be the player id from DB not entityId
	val playerId : Int
	def process() 
}