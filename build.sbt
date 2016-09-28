lazy val commonSettings = Seq(
  organization := "mwt.mockedstreams",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.8",
  description := "",
  organizationHomepage := Some(url("https://www.madewithtea.com")),
  parallelExecution in Test := false,
  scalacOptions := Seq("-Xexperimental"))

val log4jVersion = "1.2.17"
val slf4jVersion = "1.7.21"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "2.2.5"
lazy val rocksDB = "org.rocksdb" % "rocksdbjni" % "4.9.0"

lazy val logging = Seq("log4j" % "log4j" % log4jVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-log4j12" % slf4jVersion)

lazy val mockedstreams = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    assemblyJarName in assembly := "mockedstreams.jar"
  ).
  settings(
    libraryDependencies ++= Seq(
      scalaTest,
      rocksDB
    ) ++ logging
  )



