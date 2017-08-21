name := "maester-json4s"
description := "json4s support for maester's monadic decoding patterns"

val json4sVersion = settingKey[String]("json4sVersion")
json4sVersion := "3.2.+"

libraryDependencies += "org.json4s" %% "json4s-core" % json4sVersion.value
