package ayai.actions


sealed class MoveDirection(val xDirection : Int, val yDirection : Int)

case class LeftDirection extends MoveDirection(-1,0)
case class RightDirection extends MoveDirection(1,0)
case class UpDirection extends MoveDirection(0,-1)
case class DownDirection extends MoveDirection(0,1)
case class UpLeftDirection extends MoveDirection(-1,-1)
case class UpRightDirection extends MoveDirection(1,-1)
case class DownRightDirection extends MoveDirection(1,1)
case class DownLeftDirection extends MoveDirection(-1,1)
