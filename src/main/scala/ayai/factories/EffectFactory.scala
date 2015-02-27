package ayai.factories

import ayai.components._
import crane.{World, Component, Entity}
import ayai.gamestate._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.concurrent.duration._
import ayai.apps._ 
import ayai.statuseffects._

case class AllEffectValues(
      id: Int,
      name: String,
      description: String,
      effectType: String,
      value: Int,
      attribute: String,
      length: Option[Int],
      interval: Option[Int],
      multiplier: Double,
      isRelative: Boolean,
      isValueRelative: Boolean,
      image: String)

object EffectFactory {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  def bootup(networkSystem: ActorSystem): Unit = {
    val effects: List[AllEffectValues] = getEffectList("src/main/resources/effects/effects.json")
    effects.foreach(effectData => {
      val att = effectData.attribute.toLowerCase match {
        case "oneoff" => new OneOff()
        case "duration" => new ayai.statuseffects.Duration(effectData.length.get)
        case "timedinterval" => new TimedInterval(effectData.length.get, effectData.interval.get)
      }
      
      val effect = new Effect(effectData.id,
        effectData.name,
        effectData.description,
        effectData.effectType,
        effectData.value,
        att,
        new Multiplier(effectData.multiplier),
        effectData.isRelative,
        effectData.isValueRelative
      )

      effect.imageLocation = effectData.image
      networkSystem.actorSelection("user/EffectMap") ! AddEffect("Effect" + effectData.id, effect)
    })
  }

  def getEffectList(path: String): List[AllEffectValues] = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val source = scala.io.Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val parsedJson = parse(lines)

    val rootClasses = (parsedJson \\ "effects").extract[List[AllEffectValues]]

    rootClasses.toList
  }
}