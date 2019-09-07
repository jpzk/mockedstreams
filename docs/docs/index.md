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

## Apache Kafka Compatibility

Please use the corresponding Mocked Streams version to a concrete Apache Kafka version.

| Mocked Streams Version        | Apache Kafka Version           |
|------------- |-------------|
| 3.4.0      | 2.3.0.0 |
| 3.3.0      | 2.2.0.0 |
| 3.2.0      | 2.1.1.0 | 
| 3.1.0      | 2.1.0.0 | 
| 2.2.0      | 2.1.0.0 | 
| 2.1.0      | 2.0.0.0 | 
  2.0.0      | 2.0.0.0 |
| 1.8.0      | 1.1.1.0 |
| 1.7.0      | 1.1.0.0 |
| 1.6.0      | 1.0.1.0 |
| 1.5.0      | 1.0.0.0 |
| 1.4.0      | 0.11.0.1 | 
| 1.3.0      | 0.11.0.0 | 
| 1.2.1      | 0.10.2.1 | 
| 1.2.0      | 0.10.2.0 | 
| 1.1.0      | 0.10.1.1 | 
| 1.0.0      | 0.10.1.0      |    