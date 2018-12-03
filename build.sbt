
lazy val commonSettings = Seq(
  organization := "com.madewithtea",
  version := "2.2.0",
  scalaVersion := "2.12.6",
  crossScalaVersions := Seq("2.12.7", "2.11.12"),
  description := "Topology Unit-Testing Library for Apache Kafka / Kafka Streams",
  organizationHomepage := Some(url("https://www.madewithtea.com")),
  scalacOptions := Seq("-Xexperimental"))

val scalaTestVersion = "3.0.5"
val rocksDBVersion = "5.17.2"
val kafkaVersion = "2.1.0"

lazy val kafka = Seq(
  "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" jar(),
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion classifier "test",
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion,
  "org.apache.kafka" %% "kafka" % kafkaVersion
)

lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
lazy val rocksDB = "org.rocksdb" % "rocksdbjni" % rocksDBVersion % "test"

lazy val mockedstreams = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(
      scalaTest,
      rocksDB
    ) ++ kafka
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

pomExtra :=
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


