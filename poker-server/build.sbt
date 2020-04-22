name := "poker-server"

version := "0.1"

scalaVersion := "2.13.1"

Compile / mainClass := Some("com.evo.poker.PokerServerApp")

scalacOptions ++= Seq(
//  "-deprecation",
//  "-feature",
  "-Ymacro-annotations"
)

libraryDependencies ++= Seq(
  "org.typelevel"              %% "cats-core" % "2.1.0",
  "org.typelevel"              %% "cats-effect" % "2.1.0",
  "org.tpolecat"               %% "atto-core" % "0.7.2",
  "com.softwaremill.quicklens" %% "quicklens" % "1.5.0",
  "com.typesafe.akka"          %% "akka-http" % "10.1.11",
  "de.heikoseeberger"          %% "akka-http-circe" % "1.31.0",
  "com.typesafe.akka"          %% "akka-stream" % "2.6.4",
  "com.typesafe.akka"          %% "akka-actor" % "2.6.4",
  "com.typesafe.akka"          %% "akka-persistence" % "2.6.4",
  "io.circe"                   %% "circe-core" % "0.13.0",
  "io.circe"                   %% "circe-generic" % "0.13.0",
  "io.circe"                   %% "circe-generic-extras" % "0.13.0",
  "io.circe"                   %% "circe-optics" % "0.13.0",
  "io.circe"                   %% "circe-parser" % "0.13.0",
  "com.typesafe.akka"          %% "akka-testkit" % "2.6.4" % Test,
  "org.scalatest"              %% "scalatest" % "3.1.0" % Test
)
