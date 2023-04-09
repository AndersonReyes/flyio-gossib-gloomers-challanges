ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val circeVersion = "0.14.1"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-Ymacro-annotations")
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "scala-gossip-gloomers-challange"
  )
  .aggregate(echo, core)

lazy val core: Project = Project("core", file("core"))
  .settings(commonSettings)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras"
    ).map(_ % circeVersion)
  )

lazy val echo: Project = Project("echo", file("echo"))
  .settings(commonSettings)
  .settings(
    name := "echo",
    assembly / mainClass := Some("com.github.andersonreyes.Main")
  )
  .dependsOn(core)
