---
layout: docs
title: Custom Configuration
---

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

    mstreams.output("out-a", strings, ints, expA.size) shouldEqual(expectedA)
    mstreams.output("out-b", strings, ints, expB.size) shouldEqual(expectedB)
 