package ayai.maps

sealed trait Layer

case class CollidableLayer(value : Int) extends Layer()
case class NormalLayer(value : Int) extends Layer()	
