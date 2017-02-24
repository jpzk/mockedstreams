# Mocked Streams 

[![Build Status](https://travis-ci.org/jpzk/mockedstreams.svg?branch=master)](https://travis-ci.org/jpzk/mockedstreams) [![License](http://img.shields.io/:license-Apache%202-grey.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt) [![GitHub stars](https://img.shields.io/github/stars/jpzk/mockedstreams.svg?style=flat)](https://github.com/jpzk/mockedstreams/stargazers) 


Mocked Streams 1.0 [(git)](https://github.com/jpzk/mockedstreams) is a library for Scala >= 2.11.8 which allows you to **unit-test processing topologies** of [Kafka Streams](https://kafka.apache.org/documentation#streams) applications (since Apache Kafka >=0.10.1) **without Zookeeper and Kafka Brokers**. Further, you can use your favourite Scala testing framework e.g. [ScalaTest](http://www.scalatest.org/) and [Specs2](https://etorreborre.github.io/specs2/). Mocked Streams is located at the Maven Central Repository, therefore you just have to add the following to your [SBT dependencies](http://www.scala-sbt.org/0.13/docs/Library-Dependencies.html):

    libraryDependencies += "com.madewithtea" %% "mockedstreams" % "1.1.0" % "test"

## Apache Kafka Compatibility

| Mocked Streams Version        | Apache Kafka Version           | 
| ------------- |-------------| 
| 1.2.0      | 0.10.2 | 
| 1.1.0      | 0.10.1.1 | 
| 1.0.0      | 0.10.1.0      |    


## Simple Example

It wraps the [org.apache.kafka.test.ProcessorTopologyTestDriver](https://github.com/apache/kafka/blob/trunk/streams/src/test/java/org/apache/kafka/test/ProcessorTopologyTestDriver.java) class, but adds more syntactic sugar to keep your test code simple:

    import com.madewithtea.mockedstreams.MockedStreams

    val input = Seq(("x", "v1"), ("y", "v2"))
    val exp = Seq(("x", "V1"), ("y", "V2"))
    val strings = Serdes.String()

    MockedStreams()
      .topology { builder => builder.stream(...) [...] }
      .input("topic-in", strings, strings, input)
      .output("topic-out", strings, strings, exp.size) shouldEqual exp

## Multiple Input / Output Example and State

It also allows you to have multiple input and output streams. If your topology uses state stores you need to define them using .stores(Seq[String]):

    import com.madewithtea.mockedstreams.MockedStreams

    val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] }
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

    mstreams.output("out-a", strings, ints, expA.size) shouldEqual(expectedA)
    mstreams.output("out-b", strings, ints, expB.size) shouldEqual(expectedB)

## Testing the State Store Content

     val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] }
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

     mstreams.stateTable("store-name") shouldEqual Map('a' -> 1) 
 
## Custom Streams Configuration

Sometimes you need to pass a custom configuration to Kafka Streams:

    import com.madewithtea.mockedstreams.MockedStreams

      val props = new Properties
      props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[CustomExtractor].getName)

      val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] }
      .config(props)
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

    mstreams.output("out-a", strings, ints, expA.size) shouldEqual(expectedA)
    mstreams.output("out-b", strings, ints, expB.size) shouldEqual(expectedB)
 
