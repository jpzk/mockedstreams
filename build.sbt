lazy val commonSettings = Seq(
  organization := "com.madewithtea",
  version := "1.0.0",
  scalaVersion := "2.11.8",
  description := "Topology Unit-Testing Library for Apache Kafka / Kafka Streams",
  organizationHomepage := Some(url("https://www.madewithtea.com")),
  coverageEnabled := true,
  scalacOptions := Seq("-Xexperimental"))

val log4jVersion = "1.2.17"
val slf4jVersion = "1.7.21"
val scalaTestVersion = "2.2.6"
val rocksDBVersion = "4.11.2"
val kafkaVersion = "0.10.1.0"

lazy val kafka = Seq(
   "org.apache.kafka" % "kafka-clients" % kafkaVersion,
   "org.apache.kafka" % "kafka-clients" % kafkaVersion classifier "test",
   "org.apache.kafka" % "kafka-streams" % kafkaVersion,
   "org.apache.kafka" % "kafka-streams" % kafkaVersion classifier "test",
   "org.apache.kafka" %% "kafka" % kafkaVersion 
 )

lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
lazy val rocksDB = "org.rocksdb" % "rocksdbjni" % rocksDBVersion % "test"
lazy val logging = Seq("log4j" % "log4j" % log4jVersion % "test",
  "org.slf4j" % "slf4j-api" % slf4jVersion % "test",
  "org.slf4j" % "slf4j-log4j12" % slf4jVersion % "test")

lazy val mockedstreams = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    assemblyJarName in assembly := "mockedstreams.jar"
  ).
  settings(
    libraryDependencies ++= Seq(
      scalaTest,
      rocksDB
    ) ++ kafka ++ logging
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
  <url>https://www.madewithtea.com/pages/mocked-streams.html</url>
    <licenses>
      <license>
        <name>Apache License Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:jpzk/mockedstreams.git</url>
      <connection>scm:git:git@github.com:jpzk/mockedstreams.git</connection>
    </scm>
    <developers>
      <developer>
        <id>jpzk</id>
        <name>Jendrik Poloczek</name>
        <url>https://www.madewithtea.com</url>
      </developer>
    </developers>
  )


