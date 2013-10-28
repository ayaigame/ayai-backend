name := "ayai-backend"
 
version := "1.0"
 
scalaVersion := "2.9.1"

mainClass := Some("TestMain")
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"