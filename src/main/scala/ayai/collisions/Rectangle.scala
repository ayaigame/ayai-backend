package ayai.collisions

class Rectangle(val x : Int, val y : Int, val  width : Int, val height : Int) {
	
	def this(width : Int, height : Int) {
		this(0, 0, width, height)
	}

	def getX() : Int = {
		x
	}

	def getY() : Int = {
		y
	}

	def getWidth() : Int = {
		width
	}

	def getHeight() : Int = {
		height
	}
}