name := "evolution-gaming-scala-bootcamp-course-project"

version := "0.1"

scalaVersion := "2.13.1"

// Compile / mainClass := Some("com.evo.poker.app.App")

libraryDependencies ++= Seq(
  "org.typelevel"              %% "cats-core"   % "2.1.0",
  "org.typelevel"              %% "cats-effect" % "2.1.0",
  "org.tpolecat"               %% "atto-core"   % "0.7.2",
  "com.softwaremill.quicklens" %% "quicklens"   % "1.5.0",
  "org.scalatest"              %% "scalatest"   % "3.1.0" % Test
)
