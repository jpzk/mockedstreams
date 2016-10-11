# Mocked Streams (preview)
[![Build Status](https://travis-ci.org/jpzk/mockedstreams.svg?branch=master)](https://travis-ci.org/jpzk/mockedstreams) [![Coverage Status](https://coveralls.io/repos/github/jpzk/mockedstreams/badge.svg?branch=master)](https://coveralls.io/github/jpzk/mockedstreams?branch=master) [![License](http://img.shields.io/:license-Apache%202-grey.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

**Mocked Streams is a Kafka Streams testing library** for Kafka >= 0.10.1-SNAPSHOT (snapshot JARs included in /lib) which makes use of the ProcessorTopologyTestDriver, therefore **no Kafka brokers and Zookeeper needed** and tests can be run in parallel. It integrates well with any testing framework. The library will be published on the Maven repositories once the new Kafka version is released.


If you had your first hands-on Kafka Streams already you might have noticed that there is no easy way to unit-test your topologies. Even though Kafka is documented quite well, it lacks, for now, of good documentation of how to test processing graphs. In general, you can choose between running full Kafka brokers and zookeeper in your environment, running an embedded Kafka broker, [Zookeeper](https://zookeeper.apache.org/) like in this Kafka Streams [integration test](https://github.com/apache/kafka/blob/trunk/streams/src/test/java/org/apache/kafka/streams/integration/KStreamAggregationIntegrationTest.java), or you use mock producers and consumers for lightweight unit-tests of your topologies. While the first two approaches are the way to go for integration tests, we should also be able to unit-test our topologies in a simple way:

    val input = Seq(("x", "v1"), ("y", "v2"))
    val exp = Seq(("x", "V1"), ("y", "V2"))
    val strings = Serdes.String()

    MockedStreams(new UppercaseTopology())
      .topology(topology)
      .input("topic-in", strings, strings, input)
      .output("topic-out", strings, strings, exp.size) shouldEqual exp

### Mocked Streams

[Mocked Streams](https://github.com/jpzk/mockedstreams) is a library which allows you to do the latter wthout much boilerplate code and in your favourite Scala testing framework e.g. [ScalaTest](http://www.scalatest.org/) and [Specs2](https://etorreborre.github.io/specs2/). It wraps the [org.apache.kafka.test.ProcessorTopologyTestDriver](https://github.com/apache/kafka/blob/trunk/streams/src/test/java/org/apache/kafka/test/ProcessorTopologyTestDriver.java) class, but adds more syntactic sugar to keep your test code simple. The example above is testing the following topology. 

    trait MockedTopology {
      def builtBy(builder: KStreamBuilder): Unit
    }

    class UppercaseTopology extends MockedTopology {
       override def builtBy(builder: KStreamBuilder): Unit = {
          builder.stream(strings, strings, "topic-in")
          .map((k, v) => new KeyValue(k, v.toUpperCase))
          .to(strings, strings, "topic-out")
       }}

The trait MockedTopology needs to be implemented. For flexibilty reasons the KStreamBuilder is passed. In the example,  we have only one input and one output topic. However, we are able to define multiple inputs and multiple outputs. Assuming we have a topology [MultiInputOutputTopology](https://github.com/jpzk/mockedstreams/blob/master/src/test/scala/MockedStreamsSpec.scala) which consumes two input streams, does an aggregation with a local state, and sends records to two output topics, we can test it like this:

    val builder = MockedStreams()
      .topology(new MultiInputOutputTopology())
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stateStores(Seq("store-name"))

    builder.output("out-a", strings, ints, expA.size) shouldEqual(expectedA)
    builder.output("out-b", strings, ints, expB.size) shouldEqual(expectedB)
  
Note, that the order .input(...) calls is very important: When calling .output(...) Mocked Streams produces to the Kafka input topics in the same order as specified. In the example, it would produce all messages to topic "out-a" first, then to "out-b". Each output call will start an isolated run, fetching from the specified output topic. For a better understanding I like to refer to the [tests](https://github.com/jpzk/mockedstreams/blob/master/src/test/scala/MockedStreamsSpec.scala) of Mocked Streams itself. 

### Usage in the Next Release of Kafka

Personally, I work on next still unstable version 0.10.1 of Kafka. I was experiencing some issues back-porting Mocked Streams to the stable 0.10.0.1 release. Therefore, I decided to only support and distribute JARs for the next stable release. However, if you are interested in scratching your own itch, contribution & collaboration would be great! Unfortunately, for now, if you want to use it, you need to add Mocked Streams manually. But I will add it to the [Maven Repositories](https://mvnrepository.com/) when the next Kafka version is released!

