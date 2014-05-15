package ayai.networking

import ayai.persistence._
import ayai.factories.ClassFactory
import ayai.statuseffects._
import ayai.components._

/** Akka Imports **/
import akka.actor.{Actor}

/** Socko Imports **/
import org.mashupbots.socko.events.{HttpRequestEvent, HttpResponseStatus}

/** External Imports **/
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class AuthorizationProcessor extends Actor {

  Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
      Session.create(
      java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
      new H2Adapter))

  def receive = {
  case LoginPost(request: HttpRequestEvent) =>
    val headers = request.request.headers
    headers.get("Authorization") match {
    case Some(basicAuth: String) =>
      val auth = new String(new sun.misc.BASE64Decoder().decodeBuffer(basicAuth.replaceAll("Basic ","")))
      val delimiter = auth.indexOfSlice(":")
      val username = auth.slice(0, delimiter)
      val password = auth.slice(delimiter + 1, auth.length)

      // For signing cookies later
      val conf = ConfigFactory.load
      val serverConf = conf.getConfig("server")
      lazy val secret: String = serverConf.getString("secret")

      var token: String = ""

      token = AccountTable.validatePassword(username, password)

      token match {
      case "" =>
        request.response.write(HttpResponseStatus.UNAUTHORIZED)
      case _ =>
        request.response.write(HttpResponseStatus.OK, token)
      }
    case _ =>
      request.response.write(HttpResponseStatus.UNAUTHORIZED)

    }

  case RegisterPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")

    val username = content.slice(0, delimiter).replaceAll("email=", "")
    val password = content.slice(delimiter + 1, content.length).replaceAll("password=", "")
    if(AccountTable.registerUser(username, password)) {
      var token: String = ""
      token = AccountTable.validatePassword(username, password)

      token match {
        case "" =>
          request.response.write(HttpResponseStatus.UNAUTHORIZED)
        case _ =>
          request.response.write(HttpResponseStatus.OK, token)
      }
    }
    else
      request.response.write(HttpResponseStatus.CONFLICT)

  case CharactersPost(request: HttpRequestEvent) =>
    val token: String = request.request.content.toString
    var accountId = AccountTable.getAccountIdFromToken(token)

    if(accountId == -1)
      request.response.write(HttpResponseStatus.UNAUTHORIZED)
    else
      CharacterTable.characterList(request, accountId)

  case ClassListGet(request: HttpRequestEvent) =>
    request.response.write(HttpResponseStatus.OK, compact(render(ClassFactory.asJson)))

  case CreateCharacterPost(request: HttpRequestEvent) =>
    val content:String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")

    val userToken = content.slice(0, delimiter).replaceAll("token=", "")
    val characterName = content.slice(delimiter + 1, delimiter2).replaceAll("name=", "")
    val className = content.slice(delimiter2 + 1, content.length).replaceAll("class=", "")

    var accountId = AccountTable.getAccountIdFromToken(userToken)

    if(accountId == -1)
      request.response.write(HttpResponseStatus.UNAUTHORIZED)
    else
      CharacterTable.createCharacter(request, characterName, className, accountId)

  case RecoveryPost(request: HttpRequestEvent) =>
    println("RECOVERY")

  case ItemPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val userToken = content.slice(0, delimiter).replaceAll("token=","")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(1).replaceAll("id=", "").toLong
    val name = contentSplit(2).replaceAll("name=","")
    val value = contentSplit(3).replaceAll("value=","").toInt
    val weight = contentSplit(4).replaceAll("weight=","").toDouble
    val imageLocation = contentSplit(5).replaceAll("image=","")
    val itemType = contentSplit(6).replaceAll("itemtype=","")

    var item: Item = null

    itemType.toLowerCase match {
      case "weapon" => 
        val range = contentSplit(7).replaceAll("range=","").toInt
        val damage = contentSplit(8).replaceAll("damage=","").toInt
        val damageType = contentSplit(9).replaceAll("damageType=","")
        val itemT = contentSplit(10).replaceAll("type=","")
        item = new Item(id, name, value, weight, new Weapon(range, damage, damageType, itemT))
      case "armor" =>
        val slot = contentSplit(7).replaceAll("slot=","")
        val protection = contentSplit(8).replaceAll("protection=","").toInt
        val itemT = contentSplit(9).replaceAll("type=","")
        item = new Item(id, name, value, weight, new Armor(slot, protection, itemT))
      case "consumable" => 
        item = new Item(id, name, value, weight, new Consumable())

      case _ =>
        request.response.write(HttpResponseStatus.UNAUTHORIZED)
    }
    
    // adds item to the gamestate

    request.response.write(HttpResponseStatus.OK)

  case NPCPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val userToken = content.slice(0, delimiter).replaceAll("token=","")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(1).replaceAll("id=", "").toInt
    val name = contentSplit(2).replaceAll("name=","").toInt
    val faction = contentSplit(3).replaceAll("faction=","")
    val room = contentSplit(4).replaceAll("room=","").toInt
    val weapon1 = contentSplit(5).replaceAll("weapon1=","").toInt
    val helmet = contentSplit(6).replaceAll("helmet=","").toInt
    val torso = contentSplit(7).replaceAll("torso=","").toInt
    val legs = contentSplit(8).replaceAll("legs=","").toInt
    val feet = contentSplit(9).replaceAll("feet=","").toInt
    val level = contentSplit(10).replaceAll("level=","").toInt
    val experience = contentSplit(11).replaceAll("experience=","").toInt


    //get equipment ids for weapons given

    // get inventory item ids


    
    request.response.write(HttpResponseStatus.OK)

  case ClassPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val userToken = content.slice(0, delimiter).replaceAll("token=","")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(1).replaceAll("id=", "").toInt
    val name = contentSplit(2).replaceAll("name=","")
    val baseHealth = contentSplit(3).replaceAll("baseHealth=","").toInt
    val baseMana = contentSplit(4).replaceAll("baseMana=","").toInt
    val strength = contentSplit(5).replaceAll("strength=","").toInt
    val defense = contentSplit(6).replaceAll("defense=","").toInt
    val speed = contentSplit(7).replaceAll("speed=","").toInt
    val strengthLevel= contentSplit(8).replaceAll("strengthLevel=","").toInt
    val defenseLevel = contentSplit(9).replaceAll("defenseLevel=","").toInt
    val speedLevel = contentSplit(10).replaceAll("speedLevel=","").toInt
    val spriteSheetLocation = contentSplit(11).replaceAll("spritesheet=","")
    
    request.response.write(HttpResponseStatus.OK)

  case EffectPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val userToken = content.slice(0, delimiter).replaceAll("token=","")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(1).replaceAll("id=", "").toInt
    val name = contentSplit(2).replaceAll("name=","")
    val description = contentSplit(3).replaceAll("description=","")
    val effectType = contentSplit(4).replaceAll("effectType=","")
    val value:Int = contentSplit(5).replaceAll("value=","").toInt
    val attribute = contentSplit(6).replaceAll("attribute=","")

    
    //get required info needed for these 
    attribute.toLowerCase() match {
      case "oneoff" =>
        val multiplier:Double = contentSplit(7).replaceAll("multiplier=","").toDouble
        val isRelative:Boolean = contentSplit(8).replaceAll("isRelative=","").toBoolean
        val isValueRelative:Boolean = contentSplit(9).replaceAll("isValueRelative=","").toBoolean
        val image = contentSplit(10).replaceAll("imageLocation=","")      
        val attributeType: TimeAttribute = new OneOff()
        val effect = new Effect(id, name, 
                            description, effectType,
                            value, attributeType,
                            new Multiplier(multiplier), isRelative, isValueRelative)
        request.response.write(HttpResponseStatus.OK)

      case "duration" =>
        val duration:Int = contentSplit(7).replaceAll("length=","").toInt
        val multiplier:Double = contentSplit(8).replaceAll("multiplier=","").toDouble
        val isRelative:Boolean = contentSplit(9).replaceAll("isRelative=","").toBoolean
        val isValueRelative:Boolean = contentSplit(10).replaceAll("isValueRelative=","").toBoolean
        val image = contentSplit(11).replaceAll("imageLocation=","")
        val attributeType: TimeAttribute = new Duration(duration)
        val effect = new Effect(id, name, 
                            description, effectType,
                            value, attributeType,
                            new Multiplier(multiplier), isRelative, 
                            isValueRelative)
        request.response.write(HttpResponseStatus.OK)
      case "timedinterval" =>
        val duration:Int = contentSplit(7).replaceAll("length=","").toInt
        val interval:Int = contentSplit(8).replaceAll("interval=","").toInt
        val multiplier:Double = contentSplit(9).replaceAll("multiplier=","").toDouble
        val isRelative:Boolean = contentSplit(10).replaceAll("isRelative=","").toBoolean
        val isValueRelative:Boolean = contentSplit(11).replaceAll("isValueRelative=","").toBoolean
        val image = contentSplit(12).replaceAll("imageLocation=","")
        val attributeType: TimeAttribute = new TimedInterval(duration, interval)
        val effect = new Effect(id,name, 
                            description, effectType,
                            value, attributeType,
                            new Multiplier(multiplier), isRelative, isValueRelative )
        request.response.write(HttpResponseStatus.OK)
      case _ => request.response.write(HttpResponseStatus.UNAUTHORIZED)
    }   
  case _ => println("Error from AuthorizationProcessor.")
  }
}
