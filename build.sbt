
lazy val commonSettings = Seq(
  organization := "com.madewithtea",
  version := "3.4.0",
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.12.8", "2.11.12"),
  description := "Topology Unit-Testing Library for Apache Kafka / Kafka Streams",
  organizationHomepage := Some(url("https://www.madewithtea.com")),
  scalacOptions := Seq("-Xexperimental"))

val scalaTestVersion = "3.0.8"
val rocksDBVersion = "5.18.3"
val kafkaVersion = "2.3.0"

lazy val kafka = Seq(
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


micrositeName := "Mocked Streams"
micrositeDescription := "Scala Library for Unit-Testing Processing Topologies in Kafka Streams"
micrositeUrl := "http://mockedstreams.madewithtea.com"
micrositeAuthor := "Jendrik Poloczek"
micrositeBaseUrl := "/mockedstreams"
micrositeDocumentationUrl := "/mockedstreams/docs"
micrositeTwitter := "@madewithtea"
micrositeTwitterCreator := "@madewithtea"
micrositeGithubOwner := "jpzk"
micrositeGithubRepo := "mockedstreams"
micrositeCompilingDocsTool := WithMdoc

lazy val docs = project       // new documentation project
  .in(file("ms-docs")) // important: it must not be docs/
  .dependsOn(mockedstreams)
  .enablePlugins(MdocPlugin)

enablePlugins(MicrositesPlugin)