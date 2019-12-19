---
layout: home
title:  "Home"
section: "home"
technologies:
 - first: ["Scala", "sbt-microsites plugin is completely written in Scala"]
 - second: ["SBT", "sbt-microsites plugin uses SBT and other sbt plugins to generate microsites easily"]
 - third: ["Jekyll", "Jekyll allows for the transformation of plain text into static websites and blogs."]
---

[![Build Status](https://travis-ci.org/jpzk/mockedstreams.svg?branch=master)](https://travis-ci.org/jpzk/mockedstreams)   [![Codacy Badge](https://api.codacy.com/project/badge/Grade/8abac3d072e54fa3a13dc3da04754c7b)](https://www.codacy.com/app/jpzk/mockedstreams?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jpzk/mockedstreams&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/jpzk/mockedstreams/branch/master/graph/badge.svg)](https://codecov.io/gh/jpzk/mockedstreams) 

Mocked Streams 3.5.0 is a library for Scala 2.12 which allows you to **unit-test processing topologies** of [Kafka Streams](https://kafka.apache.org/documentation#streams) applications (since Apache Kafka >=0.10.1) **without Zookeeper and Kafka Brokers**. Further, you can use your favourite Scala testing framework e.g. [ScalaTest](http://www.scalatest.org/) and [Specs2](https://etorreborre.github.io/specs2/). Mocked Streams is located at the Maven Central Repository, therefore you just have to add the following to your [SBT dependencies](http://www.scala-sbt.org/0.13/docs/Library-Dependencies.html):

    libraryDependencies += "com.madewithtea" %% "mockedstreams" % "3.5.0" % "test"

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
| 3.5.0      | 2.4.0.0 |
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


## Companies using Mocked Streams 

* [Sky.uk](https://www.sky.com/)
* [moip, a wirecard company](https://moip.com.br/en/)
* [Hive Streaming AB](https://www.hivestreaming.com/)
