package ayai.statuseffects

/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

abstract class TimeAttribute {
  def process(): Boolean
  def asJson(): JObject
  def isReady: Boolean
  def initialize()
  def isValid: Boolean
}

case class OneOff() extends TimeAttribute {
  // TODO get rid of mutability or make thread-safe
  var timesUsed = 0

  def initialize(): Unit = {
    timesUsed = 0
  }

  def process(): Boolean = {
    timesUsed = 1
    true
  }

  def asJson(): JObject = {
    "type" -> "oneoff"
  }

  def isReady: Boolean = timesUsed < 1

  def isValid: Boolean = timesUsed < 1
}


// maxTime will be in seconds
// Will active every interval until maxTime
// Example if you want to process every 5 seconds for 20 seconds do an interval of 5 and maxtime of 20
case class TimedInterval(interval: Long, maxTime: Long) extends TimeAttribute {

  var timesProcessed: Int = 0
  var startTime = 0L
  var currentTime = 0L
  var endTime  = 0L

  override def initialize() {
      timesProcessed = 0
      startTime = System.currentTimeMillis
      endTime = startTime + (maxTime * 1000)
      process()
  }

  def process(): Boolean = {
      if (isReady) {
        timesProcessed += 1
        true
      } else {
        false
      }
  }

  def asJson(): JObject = {
      ("type" -> "interval") ~
      ("timeleft" -> getTimeLeft)
  }

  def isReady: Boolean = {
    val nextTimeToProcess = timesProcessed * interval
    currentTime = System.currentTimeMillis
    (currentTime - startTime) >= (startTime + ((interval * 1000) * timesProcessed))
  }

  def getTimeLeft: Long = {
      currentTime = System.currentTimeMillis
      maxTime - currentTime
  }

  def isValid: Boolean = timesProcessed == (maxTime / interval)
}

case class Duration(maxTime: Long) extends TimeAttribute() {
  var timesProcessed: Int = 0
  var startTime = 0L
  var currentTime = 0L
  var endTime = 0L
  
  def initialize() {
    timesProcessed = 0
    val time: Long = System.currentTimeMillis
    startTime = time
    endTime = time + (maxTime * 1000)
    process()    
  }

  def process(): Boolean = {
    if(isReady) {
      timesProcessed += 1
      true
    } else {
      false
    }
  }

  def asJson(): JObject = {
    ("type" -> "interval") ~
    ("timeleft" -> getTimeLeft)
  }

  def isReady: Boolean = {
    false
  }

  def getTimeLeft: Long = {
    currentTime = System.currentTimeMillis
    endTime - currentTime
  }

  def isValid: Boolean = getTimeLeft > 0
}