package ayai.statuseffects

import crane.Entity
/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

abstract class Attribute(val name: String, val description: String) {
	def process(): Boolean
	def asJson(): JObject
	def isReady(): Boolean
	def initialize()
}

case class OneOff() extends Attribute("", "") {
	var timesUsed = 0;
	override def initialize() {
		timesUsed = 0
	}

	def process(): Boolean = {
		timesUsed = 1
		true
	}

	def asJson(): JObject = {
		("type" -> "oneoff")
	}

	def isReady(): Boolean = {
		if(timesUsed >= 1) {
			false
		} else {
			true
		}
	}
}


// maxTime will be in seconds
// Will active every interval until maxTime
// Example if you want to process every 5 seconds for 20 seconds do an interval of 5 and maxtime of 20
case class TimedInterval(val name: String, 
						 val description: String,
						 val interval: Long,
						 val maxTime: Long) extends Attribute(name, description) {
	var timesProcessed: Int = 0
	var startTime: Long = 0
	var currentTime: Long = 0
	var endTime: Long = 0

	override def initialize() {
		timesProcessed = 0
		var time: Long = System.currentTimeMillis
		startTime = time
		endTime = time + (maxTime * 1000)
		process()
	}

	def process(): Boolean = {
		if(isReady()) {
			timesProcessed += 1
			true
		} else {
			false
		}
	}

	def asJson(): JObject = {
		("type" -> "interval") ~
		("name" -> name) ~
		("description" -> description) ~
		("timeleft" -> getTimeLeft)
	}

	def isReady(): Boolean = {
		nextTimeToProcess = timesProcessed * interval
		currentTime = System.currentTimeMillis
		if((currentTime - startTime) >= (startTime+((interval*1000) * timesProcessed) )) {
			true
		} else {
			false
		}
	}

	def getTimeLeft(): Long = {
		currentTime = System.currentTimeMillis
		return maxTime - currentTime
	}
}
