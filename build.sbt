lazy val commonSettings = Seq(
  organization := "mwt.mockedstreams",
  version := "1.0.0",
  scalaVersion := "2.11.8",
  description := "",
  organizationHomepage := Some(url("https://www.madewithtea.com")),
  parallelExecution in Test := false,
  coverageEnabled := true,
  scalacOptions := Seq("-Xexperimental"))

val log4jVersion = "1.2.17"
val slf4jVersion = "1.7.21"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"
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

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://your.project.url</url>
    <licenses>
      <license>
        <name>BSD-style</name>
        <url>http://www.opensource.org/licenses/bsd-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:your-account/your-project.git</url>
      <connection>scm:git:git@github.com:your-account/your-project.git</connection>
    </scm>
    <developers>
      <developer>
        <id>you</id>
        <name>Your Name</name>
        <url>http://your.url</url>
      </developer>
    </developers>
  )


