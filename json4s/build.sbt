name := "maester-json4s"
description := "json4s support for maester's monadic decoding patterns"

scalacOptions += "-feature"

val json4sVersion = settingKey[String]("json4sVersion")
json4sVersion := "3.2.+"

val scalatestVersion = settingKey[String]("scalatestVersion")
scalatestVersion := "3.0.+"

val testDependencies = settingKey[Seq[ModuleID]]("testDependencies")
testDependencies := Seq()

testDependencies += "org.scalatest" %% "scalatest" % scalatestVersion.value
testDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.+"

libraryDependencies += "org.json4s" %% "json4s-core" % json4sVersion.value
libraryDependencies ++= testDependencies.value map(_ % "test")

fork in Test := true
