package ayai.collisions

class QuadTree(pLevel : Int, Rectangle pBounds) {
	private val MAX_OBJECTS : Int = 10 
	private val MAX_LEVELS : Int = 5

	private val objects : List[Entity]
	private val nodes  = new QuadTree[4]

	def clear() {
		objects.clear
	}
}