# Mocked Streams 

[![Build Status](https://travis-ci.org/jpzk/mockedstreams.svg?branch=master)](https://travis-ci.org/jpzk/mockedstreams)   [![Codacy Badge](https://api.codacy.com/project/badge/Grade/8abac3d072e54fa3a13dc3da04754c7b)](https://www.codacy.com/app/jpzk/mockedstreams?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jpzk/mockedstreams&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/jpzk/mockedstreams/branch/master/graph/badge.svg)](https://codecov.io/gh/jpzk/mockedstreams) [![License](http://img.shields.io/:license-Apache%202-grey.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt) [![GitHub stars](https://img.shields.io/github/stars/jpzk/mockedstreams.svg?style=flat)](https://github.com/jpzk/mockedstreams/stargazers) 
[![Maven Central](https://img.shields.io/maven-central/v/com.madewithtea/mockedstreams_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Cmockedstreams) 

Documentation located at http://mockedstreams.madewithtea.com/

Mocked Streams 3.9.0 [(git)](https://github.com/jpzk/mockedstreams) is a library for Scala 2.12 and 2.13 which allows you to **unit-test processing topologies** of [Kafka Streams](https://kafka.apache.org/documentation#streams) applications (since Apache Kafka >=0.10.1) **without Zookeeper and Kafka Brokers**. Further, you can use your favourite Scala testing framework e.g. [ScalaTest](http://www.scalatest.org/) and [Specs2](https://etorreborre.github.io/specs2/). Mocked Streams is located at the Maven Central Repository, therefore you just have to add the following to your [SBT dependencies](http://www.scala-sbt.org/0.13/docs/Library-Dependencies.html):

    libraryDependencies += "com.madewithtea" %% "mockedstreams" % "3.9.0" % "test"

Java 8 port of Mocked Streams is [Mockafka](https://github.com/carlosmenezes/mockafka)

## Apache Kafka Compatibility

| Mocked Streams Version        | Apache Kafka Version           |
|------------- |-------------|
| 3.9.0     | 2.7.0.0 |
| 3.8.0     | 2.6.1.0 |
| 3.7.0     | 2.5.0.0 |
| 3.6.0      | 2.4.1.0 |
| 3.5.2      | 2.4.0.0 |
| 3.5.1      | 2.4.0.0 |
| 3.5.0      | 2.4.0.0 |
| 3.4.0      | 2.3.0.0 |
| 3.3.0      | 2.2.0.0 |
| 3.2.0      | 2.1.1.0 | 
| 3.1.0      | 2.1.0.0 | 
| 2.2.0      | 2.1.0.0 | 
| 2.1.0      | 2.0.0.0 | 
| 2.0.0      | 2.0.0.0 |
| 1.8.0      | 1.1.1.0 |
| 1.7.0      | 1.1.0.0 |
| 1.6.0      | 1.0.1.0 |
| 1.5.0      | 1.0.0.0 |
| 1.4.0      | 0.11.0.1 | 
| 1.3.0      | 0.11.0.0 | 
| 1.2.1      | 0.10.2.1 | 
| 1.2.0      | 0.10.2.0 | 
| 1.1.0      | 0.10.1.1 | 
| 1.0.0      | 0.10.1.0 |    

## Simple Example

It wraps the [org.apache.kafka.streams.TopologyTestDriver](https://github.com/apache/kafka/blob/trunk/streams/test-utils/src/main/java/org/apache/kafka/streams/TopologyTestDriver.java) class, but adds more syntactic sugar to keep your test code simple:

    import com.madewithtea.mockedstreams.MockedStreams

    val input = Seq(("x", "v1"), ("y", "v2"))
    val exp = Seq(("x", "V1"), ("y", "V2"))
    val strings = Serdes.String()

    MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("topic-in", strings, strings, input)
      .output("topic-out", strings, strings) shouldEqual exp

## Multiple Input / Output Example and State

It also allows you to have multiple input and output streams. If your topology uses state stores you need to define them using .stores(stores: Seq[String]):

    import com.madewithtea.mockedstreams.MockedStreams

    val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

    mstreams.output("out-a", strings, ints) shouldEqual(expectedA)
    mstreams.output("out-b", strings, ints) shouldEqual(expectedB)

## Record order and multiple emissions

The records provided to the mocked stream will be submitted to the topology during the test in the order in which they appear in the fixture. You can also submit records multiple times to the same topics, at various moments in your scenario. 

This can be handy to validate that your topology behaviour is or is not dependent on the order in which the records are received and processed. 

In the example below, 2 records are first submitted to topic A, then 3 to topic B, then 1 more to topic A again. 

    val firstInputForTopicA = Seq(("x", int(1)), ("y", int(2)))
    val firstInputForTopicB = Seq(("x", int(4)), ("y", int(3)), ("y", int(5)))
    val secondInputForTopicA = Seq(("y", int(4)))

    val expectedOutput = Seq(("x", 5), ("y", 5), ("y", 7), ("y", 9))

    val builder = MockedStreams()
      .topology(topologyTables) // Scala DSL
      .input(InputATopic, strings, ints, firstInputForTopicA)
      .input(InputBTopic, strings, ints, firstInputForTopicB)
      .input(InputATopic, strings, ints, secondInputForTopicA)

## State Store 

When you define your state stores via .stores(stores: Seq[String]) since 1.2, you are able to verify the state store content via the .stateTable(name: String) method:  

    import com.madewithtea.mockedstreams.MockedStreams

     val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

     mstreams.stateTable("store-name") shouldEqual Map('a' -> 1) 

## Window State Store 

When you define your state stores via .stores(stores: Seq[String]) since 1.2 and added the timestamp extractor to the config, you are able to verify the window state store content via the .windowStateTable(name: String, key: K) method:  

    import com.madewithtea.mockedstreams.MockedStreams

    val props = new Properties
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
      classOf[TimestampExtractors.CustomTimestampExtractor].getName)

    val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("in-a", strings, ints, inputA)
      .stores(Seq("store-name"))
      .config(props)

    mstreams.windowStateTable("store-name", "x") shouldEqual someMapX
    mstreams.windowStateTable("store-name", "y") shouldEqual someMapY

## Adding Timestamps

With .input the input records timestamps are set to 0 default timestamp of 0. This e.g. prevents testing Join windows of Kafka streams as it cannot produce records with different timestamps. However, using .inputWithTime allows adding timestamps like in the following example: 

    val inputA = Seq(
      ("x", int(1), 1000L),
      ("x", int(1), 1001L),
      ("x", int(1), 1002L)
    )

    val builder = MockedStreams()
      .topology(topology1WindowOutput) // Scala DSL
      .inputWithTime(InputCTopic, strings, ints, inputA)
      .stores(Seq(StoreName))


## Custom Streams Configuration

Sometimes you need to pass a custom configuration to Kafka Streams:

    import com.madewithtea.mockedstreams.MockedStreams

      val props = new Properties
      props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[CustomExtractor].getName)

      val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .config(props)
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

    mstreams.output("out-a", strings, ints) shouldEqual(expectedA)
    mstreams.output("out-b", strings, ints) shouldEqual(expectedB)
 
## Companies using Mocked Streams 

* [BlaBlaCar](https://medium.com/blablacar-tech)
* [PMU.fr](https://www.pmu.fr/)
* [Sky.uk](https://www.sky.com/)
* [Hive Streaming AB](https://www.hivestreaming.com/)
* [moip, a wirecard company](https://moip.com.br/en/)
