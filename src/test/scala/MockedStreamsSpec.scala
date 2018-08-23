/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package com.madewithtea.mockedstreams

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.processor.TimestampExtractor
import org.apache.kafka.streams._
import org.scalatest.{FlatSpec, Matchers}

class MockedStreamsSpec extends FlatSpec with Matchers {

  behavior of "MockedStreams"

  it should "throw exception when expected size in output methods is <= 0" in {
    import Fixtures.Uppercase._
    import MockedStreams.ExpectedOutputIsEmpty

    val spec = MockedStreams()
      .topology(topology)
      .input(InputTopic, strings, strings, input)

    Seq(-1, 0).foreach { size =>
      an[ExpectedOutputIsEmpty] should be thrownBy
        spec.output(OutputTopic, strings, strings, size)

      an[ExpectedOutputIsEmpty] should be thrownBy
        spec.outputTable(OutputTopic, strings, strings, size)
    }
  }

  it should "throw exception when no input specified for all output and state methods" in {
    import Fixtures.Uppercase._
    import MockedStreams.NoInputSpecified

    val t = MockedStreams().topology(topology)

    an[NoInputSpecified] should be thrownBy
      t.output(OutputTopic, strings, strings, expected.size)

    an[NoInputSpecified] should be thrownBy
      t.outputTable(OutputTopic, strings, strings, expected.size)

    an[NoInputSpecified] should be thrownBy
      t.stateTable("state-table")

    an[NoInputSpecified] should be thrownBy
      t.windowStateTable("window-state-table", 0)
  }

  it should "assert correctly when processing strings to uppercase" in {
    import Fixtures.Uppercase._

    val output = MockedStreams()
      .topology(topology)
      .input(InputTopic, strings, strings, input)
      .output(OutputTopic, strings, strings, expected.size)

    output shouldEqual expected
  }

  it should "assert correctly when processing strings to uppercase match against table" in {
    import Fixtures.Uppercase._

    val output = MockedStreams()
      .topology(topology)
      .input(InputTopic, strings, strings, input)
      .outputTable(OutputTopic, strings, strings, expected.size)

    output shouldEqual expected.toMap
  }

  it should "assert correctly when processing multi input topology" in {
    import Fixtures.Multi._

    val builder = MockedStreams()
      .topology(topology1Output)
      .input(InputATopic, strings, ints, inputA)
      .input(InputBTopic, strings, ints, inputB)
      .stores(Seq(StoreName))

    builder.output(OutputATopic, strings, ints, expectedA.size) shouldEqual expectedA
    builder.stateTable(StoreName) shouldEqual inputA.toMap
  }

  it should "assert correctly when processing multi input output topology" in {
    import Fixtures.Multi._

    val builder = MockedStreams()
      .topology(topology2Output)
      .input(InputATopic, strings, ints, inputA)
      .input(InputBTopic, strings, ints, inputB)
      .stores(Seq(StoreName))

    builder.output(OutputATopic, strings, ints, expectedA.size)
      .shouldEqual(expectedA)

    builder.output(OutputBTopic, strings, ints, expectedB.size)
      .shouldEqual(expectedB)

    builder.stateTable(StoreName) shouldEqual inputA.toMap
  }

  it should "assert correctly when joining events sent to 2 Ktables in a specific order" in {
    import Fixtures.Multi._

    val firstInputForTopicA = Seq(("x", int(1)), ("y", int(2)))
    val firstInputForTopicB = Seq(("x", int(4)), ("y", int(3)), ("y", int(5)))
    val secondInputForTopicA = Seq(("y", int(4)))

    val expectedOutput = Seq(("x", 5), ("y", 5), ("y", 7), ("y", 9))

    val builder = MockedStreams()
      .topology(topologyTables)
      .input(InputATopic, strings, ints, firstInputForTopicA)
      .input(InputBTopic, strings, ints, firstInputForTopicB)
      .input(InputATopic, strings, ints, secondInputForTopicA)

    builder.output(OutputATopic, strings, ints, expectedOutput.size)
      .shouldEqual(expectedOutput)
  }

  it should "assert correctly when processing windowed state output topology" in {
    import Fixtures.Multi._
    import java.util.Properties

    val props = new Properties
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
      classOf[TimestampExtractors.CustomTimestampExtractor].getName)

    val builder = MockedStreams()
      .topology(topology1WindowOutput)
      .input(InputCTopic, strings, ints, inputC)
      .stores(Seq(StoreName))
      .config(props)

    builder.windowStateTable(StoreName, "x")
      .shouldEqual(expectedCx.toMap)

    builder.windowStateTable(StoreName, "y")
      .shouldEqual(expectedCy.toMap)
  }

  it should "accept already built topology" in {
    import Fixtures.Uppercase._

    def getTopology() = {
      val builder = new StreamsBuilder()
      topology(builder)
      builder.build()
    }

    val output = MockedStreams()
      .withTopology(getTopology)
      .input(InputTopic, strings, strings, input)
      .output(OutputTopic, strings, strings, expected.size)

    output shouldEqual expected
  }


  it should "accept consumer records with custom timestamps" in {

    import Fixtures.Multi._

    val builder = MockedStreams()
      .topology(topology1WindowOutput)
      .inputWithTimeStamps(InputCTopic, strings, ints, inputCWithTimeStamps)
      .stores(Seq(StoreName))

    builder.windowStateTable(StoreName, "x")
      .shouldEqual(expectedCWithTimeStamps.toMap)
  }

  class LastInitializer extends Initializer[Integer] {
    override def apply() = 0
  }

  class LastAggregator extends Aggregator[String, Integer, Integer] {
    override def apply(k: String, v: Integer, t: Integer) = v
  }

  class AddJoiner extends ValueJoiner[Integer, Integer, Integer] {
    override def apply(v1: Integer, v2: Integer) = v1 + v2
  }

  class SubJoiner extends ValueJoiner[Integer, Integer, Integer] {
    override def apply(v1: Integer, v2: Integer) = v1 - v2
  }

  object Fixtures {

    object Uppercase {
      val input = Seq(("x", "v1"), ("y", "v2"))
      val expected = Seq(("x", "V1"), ("y", "V2"))

      val strings = Serdes.String()
      val serdes = Consumed.`with`(strings, strings)

      val InputTopic = "input"
      val OutputTopic = "output"

      def topology(builder: StreamsBuilder) = {
        builder.stream(InputTopic, serdes)
          .map[String, String]((k, v) => new KeyValue(k, v.toUpperCase))
          .to(OutputTopic, Produced.`with`(strings, strings))
      }
    }

    object Multi {

      def int(i: Int) = Integer.valueOf(i)

      val inputA = Seq(("x", int(1)), ("y", int(2)))
      val inputB = Seq(("x", int(4)), ("y", int(3)))
      val inputC = Seq(("x", int(1)), ("x", int(1)), ("x", int(2)), ("y", int(1)))

      val inputCWithTimeStamps = Seq(
        ("x", int(1), 1000L),
        ("x", int(1), 1000L),
        ("x", int(1), 1001L),
        ("x", int(1), 1001L),
        ("x", int(1), 1002L)
      )

      val expectedA = Seq(("x", int(5)), ("y", int(5)))
      val expectedB = Seq(("x", int(3)), ("y", int(1)))

      val expectedCx = Seq((1, 2), (2, 1))
      val expectedCy = Seq((1, 1))

      val expectedCWithTimeStamps = Seq(
        1000 -> 2,
        1001 -> 2,
        1002 -> 1
      )

      val strings = Serdes.String()
      val ints = Serdes.Integer()
      val serdes = Consumed.`with`(strings, ints)

      val InputATopic = "inputA"
      val InputBTopic = "inputB"
      val InputCTopic = "inputC"
      val OutputATopic = "outputA"
      val OutputBTopic = "outputB"
      val StoreName = "store"
      val Store2Name = "store2"

      def topology1Output(builder: StreamsBuilder) = {
        val streamA = builder.stream(InputATopic, serdes)
        val streamB = builder.stream(InputBTopic, serdes)

        val table = streamA.groupByKey(Serialized.`with`(strings, ints))
          .aggregate(
            new LastInitializer,
            new LastAggregator,
            Materialized.as(StoreName).withKeySerde(strings).withValueSerde(ints)
          )

        streamB.leftJoin[Integer, Integer](table, new AddJoiner(), Joined.`with`(strings, ints, ints))
          .to(OutputATopic, Produced.`with`(strings, ints))
      }

      def topology1WindowOutput(builder: StreamsBuilder) = {
        val streamA = builder.stream(InputCTopic, serdes)
        streamA.groupByKey(Serialized.`with`(strings, ints))
          .windowedBy(TimeWindows.of(1))
          .count(Materialized.as(StoreName))
      }

      def topology2Output(builder: StreamsBuilder) = {
        val streamA = builder.stream(InputATopic, serdes)
        val streamB = builder.stream(InputBTopic, serdes)

        val table = streamA.groupByKey(Serialized.`with`(strings, ints)).aggregate(
          new LastInitializer,
          new LastAggregator,
          Materialized.as(StoreName).withKeySerde(strings).withValueSerde(ints))

        streamB.leftJoin[Integer, Integer](table, new AddJoiner(), Joined.`with`(strings, ints, ints))
          .to(OutputATopic, Produced.`with`(strings, ints))

        streamB.leftJoin[Integer, Integer](table, new SubJoiner(), Joined.`with`(strings, ints, ints))
          .to(OutputBTopic, Produced.`with`(strings, ints))
      }

      def topologyTables(builder: StreamsBuilder) = {
        val streamA = builder.stream(InputATopic, serdes)
        val streamB = builder.stream(InputBTopic, serdes)

        val tableA = streamA.groupByKey(Serialized.`with`(strings, ints))
          .aggregate(
            new LastInitializer,
            new LastAggregator,
            Materialized.as(StoreName).withKeySerde(strings).withValueSerde(ints)
          )

        val tableB = streamB.groupByKey(Serialized.`with`(strings, ints))
          .aggregate(
            new LastInitializer,
            new LastAggregator,
            Materialized.as(Store2Name).withKeySerde(strings).withValueSerde(ints)
          )

        val resultTable = tableA.join[Integer,Integer](tableB, new AddJoiner)

        resultTable
          .toStream
          .to(OutputATopic, Produced.`with`(strings, ints))
      }
    }

  }

}

object TimestampExtractors {

  class CustomTimestampExtractor extends TimestampExtractor {
    override def extract(record: ConsumerRecord[AnyRef, AnyRef], previous: Long) = record.value match {
      case value: Integer => value.toLong
      case _ => record.timestamp()
    }
  }

}
