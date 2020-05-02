scalaVersion := "2.13.1"
scalacOptions ++= Seq( "-Ymacro-annotations" )

enablePlugins(JavaAppPackaging)

name := "poker-api"
version := "0.1"

libraryDependencies ++= Seq(
  "org.typelevel"              %% "cats-core" % "2.1.0",
  "org.typelevel"              %% "cats-effect" % "2.1.0",
  "org.tpolecat"               %% "atto-core" % "0.7.2",
  "com.softwaremill.quicklens" %% "quicklens" % "1.5.0",
  "com.typesafe.akka"          %% "akka-http" % "10.1.11",
  "com.typesafe.akka"          %% "akka-slf4j" % "2.6.4",
  "com.typesafe.akka"          %% "akka-stream" % "2.6.4",
  "com.typesafe.akka"          %% "akka-actor" % "2.6.4",
  "com.pauldijou"              %% "jwt-circe" % "4.2.0",
  "ch.megard"                  %% "akka-http-cors" % "0.4.3",
  "org.mindrot"                 % "jbcrypt" % "0.3m",
  "ch.qos.logback"              % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "de.heikoseeberger"          %% "akka-http-circe" % "1.31.0",
  "io.circe"                   %% "circe-core" % "0.13.0",
  "io.circe"                   %% "circe-generic" % "0.13.0",
  "io.circe"                   %% "circe-generic-extras" % "0.13.0",
  "io.circe"                   %% "circe-parser" % "0.13.0",
  "com.softwaremill.macwire"   %% "macros" % "2.3.3" % "provided",
  "com.softwaremill.macwire"   %% "macrosakka" % "2.3.3" % "provided",
  "com.softwaremill.macwire"   %% "util" % "2.3.3",
  "org.mongodb.scala"          %% "mongo-scala-driver" % "2.9.0",
  "com.typesafe.akka"          %% "akka-testkit" % "2.6.4" % Test,
  "com.typesafe.akka"          %% "akka-http-testkit" % "10.1.11" % Test,
  "org.scalatest"              %% "scalatest" % "3.1.0" % Test
)
