import com.typesafe.sbt.SbtStartScript
seq(SbtStartScript.startScriptForClassesSettings: _*)

name := "ayai-backend"
 
version := "1.0"
 
scalaVersion := "2.10.3"

//CHANGE THIS LINE TO RUN A DIFFERENT PROJECT
mainClass in (Compile, run) := Some("ayai.apps.GameLoop")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/" 

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.5.1"

libraryDependencies += "org.mashupbots.socko" %% "socko-webserver" % "0.4.0"

libraryDependencies += "log4j" % "log4j" % "1.2.12"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.2.3" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"


libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.3.166"
)

unmanagedBase := baseDirectory.value / "lib/artemis"
