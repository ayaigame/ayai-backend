package ayai.systems

import ayai.components._
import ayai.actions._
import ayai.gamestate.RoomWorld
import ayai.maps.Tile
import ayai.networking.{AttackMessage, ProcessMessage}

import java.rmi.server.UID

import crane.{Entity, System}

import akka.actor.ActorSystem

import scala.math.abs
import scala.collection.mutable._

object GoalSystem {
  def apply(actorSystem: ActorSystem) = new GoalSystem(actorSystem)
}

class GoalSystem(actorSystem: ActorSystem) extends System {
  def getScore(current: Position, goal: Position): Int = {
    abs(current.x - goal.x) + abs(current.y - goal.y)
  }

  def findDirection(entity: Entity, tp: Position): MoveDirection = {
    (entity.getComponent(classOf[Position]): @unchecked) match {
      case Some(ep: Position) =>
        val xDistance = ep.x - tp.x
        val yDistance = ep.y - tp.y

        (abs(xDistance) > abs(yDistance), xDistance > 0, yDistance > 0) match {
          case(true, true, _) =>
            LeftDirection
          case(true, false, _) =>
            RightDirection
          case(false, _, true) =>
            UpDirection
          case(false, _, false) =>
            DownDirection
          case _ =>
            new MoveDirection(0,0)
        }
      case _ =>
        new MoveDirection(0,0)
    }
  }

  override def process(delta: Int) {
    val entities = world.getEntitiesWithExclusions(include=List(classOf[Goal], classOf[Position], classOf[Actionable], classOf[Character]), exclude=List(classOf[Dead]))
    for(entity <- entities) {
      val goal = (entity.getComponent(classOf[Goal]): @unchecked) match {
        case Some(g: Goal) => g.goal
      }

      val position = goal match {
        case mt: MoveTo =>
          mt.position
        case at: AttackTo =>
          at.position
        case _ =>
          new Position(0,0)
      }

      val direction = findDirection(entity, position)
      (entity.getComponent(classOf[Actionable]), entity.getComponent(classOf[Character]), entity.getComponent(classOf[Position])) match {
        case (Some(actionable: Actionable), Some(character: Character), Some(ep: Position)) => {
          goal match {
            case mt: MoveTo =>
                // val tp = mt.position
                // actionable.active = !(ep.x == tp.x && ep.y == tp.y)
                // actionable.action = direction
            case at: AttackTo => {
                val tp = at.position
                actionable.active = !(ep.x == tp.x && ep.y == tp.y)
                actionable.active = false
                actionable.action = direction
                if (getScore(ep, tp) < 64) {

                  val bulletId = (new UID()).toString

                  entity match {
                    case initiator: Entity => {
                      (initiator.getComponent(classOf[Position]),
                      initiator.getComponent(classOf[Actionable]),
                      initiator.getComponent(classOf[Character]),
                      initiator.getComponent(classOf[Room]),
                      initiator.getComponent(classOf[Cooldown]),
                      initiator.getComponent(classOf[Dead])) match {
                        case(Some(pos: Position), Some(a: Actionable), Some(c: Character), Some(r: Room), None, None) => {

                          val m = a.action match {
                            case (move: MoveDirection) => move
                            case _ =>
                              println("Not match for movedirection")
                              new MoveDirection(0, 0)
                          }

                          //get the range of the character's weapon
                          val weaponRange = initiator.getComponent(classOf[Equipment]) match {
                            case Some(e: Equipment) => e.equipmentMap("weapon1").itemType match {
                              case weapon: Weapon => weapon.range
                              case _ => 30
                            }
                            case _ => 30
                          }

                          val upperLeftx = pos.x
                          val upperLefty = pos.y

                          val xDirection = m.xDirection
                          val yDirection = m.yDirection

                          val topLeftOfAttackx = ((weaponRange) * xDirection) + upperLeftx
                          val topLeftOfAttacky = ((weaponRange) * yDirection) + upperLefty


                          val p: Entity = world.createEntity("ATTACK"+bulletId)

                          p.components += (new Position(topLeftOfAttackx, topLeftOfAttacky))
                          p.components += (new Bounds(weaponRange, weaponRange))
                          p.components += (new Attack(initiator));
                          p.components += (new Frame(30))


                          initiator.components += (new Cooldown(System.currentTimeMillis(), 1000))

                        //p.components += (c)
                          world.addEntity(p)
                        }

                        case _ =>
                          // println("GoalSystem: Cooldown is present, cannot attack")
                      }
                    }
                    case _ =>
                  }
                } //endif
              }
            case _ => println("Goal not found.")
            }
        }
      case _ =>
      }//endmatch
    }//endfor
  }
}
