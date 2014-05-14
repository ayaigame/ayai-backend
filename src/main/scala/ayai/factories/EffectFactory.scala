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

  def bootup(networkSystem: ActorSystem) = {
    var effects: List[AllEffectValues] = getEffectList("src/main/resources/effects/effects.json")

  }

  def getEffectList(path: String): List[AllEffectValues] = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val source = scala.io.Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val parsedJson = parse(lines)

    val rootClasses = (parsedJson \\ "effects").extract[List[AllEffectValues]]

    // val listOfLists: List[List[Quest]] = rootClasses.map((path: String) => getClassesList(path))
    
    var effectList = new ArrayBuffer[AllEffectValues]()
    effectList.appendAll(rootClasses)
    effectList.toList
    // listOfLists.foreach(e => questList.appendAll(e))
    // questList.toList
  }
}