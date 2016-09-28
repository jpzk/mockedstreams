# Mocked Streams [![Build Status](https://travis-ci.org/jpzk/mockedstreams.svg?branch=master)](https://travis-ci.org/jpzk/mockedstreams) [![Coverage Status](https://coveralls.io/repos/github/jpzk/mockedstreams/badge.svg?branch=master)](https://coveralls.io/github/jpzk/mockedstreams?branch=master)

**Mocked Streams is a Kafka Streams testing library** for Kafka >= 0.10.1-SNAPSHOT (snapshot JARs included in /lib) which makes use of the ProcessorTopologyTestDriver, therefore **no Kafka brokers and Zookeeper needed** and tests can be run in parallel. It integrates well with any testing framework. The library will be published on the Maven repositories once the new Kafka version is released.

The following example shows a topology with multiple inputs and output streams. See [here](https://github.com/jpzk/mockedstreams/blob/master/src/test/scala/MockedStreamsSpec.scala) for the definition of the topology.

    [...]
    it should "assert correctly when processing multi input output topology" in {
      import Fixtures.Multi._
      
      val strings = Serdes.String()
      val ints = Serdes.Integer()

      val topology = new MultiInputOutputTopology()
      val builder = MockedStreams()
        .topology(topology)
        .input[String, Integer](topology.InputA, strings, ints, inputA)
        .input[String, Integer](topology.InputB, strings, ints, inputB)
        .stateStores(Seq(topology.Store))

      builder.output(topology.OutputA, strings, ints, expectedA.size)
        .shouldEqual(expectedA)

      builder.output(topology.OutputB, strings, ints, expectedB.size)
        .shouldEqual(expectedB)
     }
    [...]   
