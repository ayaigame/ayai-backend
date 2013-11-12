name := "ayai-backend"
 
version := "1.0"
 
scalaVersion := "2.10.3"

mainClass in (Compile, run) := Some("ayai.main.TestMain")
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.5.1"
