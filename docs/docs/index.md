---
layout: docs
title: Getting started
---

## Getting Started

It wraps the [org.apache.kafka.streams.TopologyTestDriver](https://github.com/apache/kafka/blob/trunk/streams/test-utils/src/main/java/org/apache/kafka/streams/TopologyTestDriver.java) class, but adds more syntactic sugar to keep your test code simple:

    import com.madewithtea.mockedstreams.MockedStreams

    val input = Seq(("x", "v1"), ("y", "v2"))
    val exp = Seq(("x", "V1"), ("y", "V2"))
    val strings = Serdes.String()

    MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("topic-in", strings, strings, input)
      .output("topic-out", strings, strings, exp.size) shouldEqual exp


