scalaVersion in ThisBuild := "2.12.3"
crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.11")

organization in ThisBuild := "mcoffin"

homepage in ThisBuild := Some(url("https://github.com/mcoffin/maester"))

scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/mcoffin/maester"),
    "scm:git:git://github.com/mcoffin/maester.git",
    Some("scm:git:ssh://git@github.com/mcoffin/maester.git")
  )
)

developers in ThisBuild := List(
  Developer(
    id = "mcoffin",
    name = "Matt Coffin",
    email = "mcoffin13@gmail.com",
    url = url("https://github.com/mcoffin")
  )
)

licenses in ThisBuild := Seq(("MIT" -> url("https://github.com/mcoffin/maester/blob/master/LICENSE")))

publishMavenStyle in ThisBuild := true


publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org"
  if (isSnapshot.value)
    Some("snapshots" at s"$nexus/content/repositories/snapshots")
  else
    Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
}

useGpg in ThisBuild := true

lazy val core = project in file("./core")
lazy val json4s = project in file("./json4s") dependsOn(
  core
)
