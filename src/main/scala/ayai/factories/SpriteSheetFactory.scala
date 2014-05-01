package ayai.factories

import ayai.components._
import ayai.gamestate._

/** Crane Imports **/
import crane.{Entity, World}

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction._
// import net.liftweb.json.Printer._

import scala.collection.mutable._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case class NoSpriteAvailableException(smth:String)  extends Exception(smth: String)

object SpriteSheetFactory {
  def hasSpriteSheet(weaponName: String): Boolean = {
    weaponName == "Magic Staff" || weaponName == "Elven Bow"
  }

  def getSpriteSheet(weaponName: String, xDirection: Int, yDirection: Int): SpriteSheet = {
    println(weaponName)
    if(weaponName == "Magic Staff") {
      val animations = new ArrayBuffer[Animation]()
      animations += new Animation("facedown", 0, 0 )
      new SpriteSheet("fireball", animations, 32 ,32)
    }
    else if(weaponName == "Elven Bow") {
      var frame = 0

      if(xDirection == 0 && yDirection < 0)
        frame = 1
      else if(xDirection == 0 && yDirection > 0)
        frame = 0
      else if(xDirection > 0 && yDirection == 0)
        frame = 3
      else if(xDirection < 0 && yDirection == 0)
        frame = 2
      else if(xDirection > 0 && yDirection < 0)
        frame = 6
      else if(xDirection < 0 && yDirection > 0)
        frame = 5
      else if(xDirection > 0 && yDirection > 0)
        frame = 4
      else if(xDirection < 0 && yDirection < 0)
        frame = 7


      val animations = new ArrayBuffer[Animation]()
      animations += new Animation("facedown", frame, frame )
      new SpriteSheet("arrows", animations, 24 ,24)
    }
    else throw new NoSpriteAvailableException(s"No sprite found for weapon: $weaponName")
  }
}
