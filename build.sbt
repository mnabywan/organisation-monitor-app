name := "OrganisationMonitorApp"
 
version := "1.0" 
      
lazy val `organisationmonitorapp` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.5.0"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.13.3"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.13.3"
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0"
libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-play" % "0.19.0",
  "com.pauldijou" %% "jwt-core" % "0.19.0",
  "com.auth0" % "jwks-rsa" % "0.6.1"
)
//libraryDependencies += "org.mongodb" %% "casbah" % "3.1.1"


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      