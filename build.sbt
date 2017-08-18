scalaVersion in ThisBuild := "2.12.3"
crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.11")

organization in ThisBuild := "mcoffin"

lazy val core = project in file("./core")
lazy val json4s = project in file("./json4s") dependsOn(
  core
)
