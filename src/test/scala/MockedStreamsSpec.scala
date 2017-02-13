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

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.{Aggregator, Initializer, KStreamBuilder, ValueJoiner}
import org.scalatest.{FlatSpec, Matchers}

class MockedStreamsSpec extends FlatSpec with Matchers {

  behavior of "MockedStreams"

  it should "throw exception when expected size is <= 0" in {
    import Fixtures.Uppercase._
    import MockedStreams.ExpectedOutputIsEmpty

    an[ExpectedOutputIsEmpty] should be thrownBy
      MockedStreams()
        .topology(topology _)
        .input(InputTopic, strings, strings, input)
        .output(OutputTopic, strings, strings, 0)

    an[ExpectedOutputIsEmpty] should be thrownBy
      MockedStreams()
        .topology(topology _)
        .input(InputTopic, strings, strings, input)
        .output(OutputTopic, strings, strings, -1)
  }

  it should "throw exception when no input specified" in {
    import Fixtures.Uppercase._
    import MockedStreams.NoInputSpecified

    an[NoInputSpecified] should be thrownBy
      MockedStreams()
        .topology(topology _)
        .output(OutputTopic, strings, strings, expected.size)
  }

  it should "assert correctly when processing strings to uppercase" in {
    import Fixtures.Uppercase._

    val output = MockedStreams()
      .topology(topology _)
      .input(InputTopic, strings, strings, input)
      .output(OutputTopic, strings, strings, expected.size)

    output shouldEqual expected
  }

  it should "assert correctly when processing strings to uppercase match against table" in {
    import Fixtures.Uppercase._

    val output = MockedStreams()
      .topology(topology _)
      .input(InputTopic, strings, strings, input)
      .outputTable(OutputTopic, strings, strings, expected.size)

    output shouldEqual expected.toMap
  }

  it should "assert correctly when processing multi input topology" in {
    import Fixtures.Multi._

    val builder = MockedStreams()
      .topology(topology1Output _)
      .input(InputATopic, strings, ints, inputA)
      .input(InputBTopic, strings, ints, inputB)
      .stores(Seq(StoreName))

    builder.output(OutputATopic, strings, ints, expectedA.size) shouldEqual expectedA
    builder.stateTable(StoreName) shouldEqual inputA.toMap
  }

  it should "assert correctly when processing multi input output topology" in {
    import Fixtures.Multi._

    val builder = MockedStreams()
      .topology(topology2Output _)
      .input(InputATopic, strings, ints, inputA)
      .input(InputBTopic, strings, ints, inputB)
      .stores(Seq(StoreName))

    builder.output(OutputATopic, strings, ints, expectedA.size)
      .shouldEqual(expectedA)

    builder.output(OutputBTopic, strings, ints, expectedB.size)
      .shouldEqual(expectedB)

    builder.stateTable(StoreName)
      .shouldEqual(inputA.toMap)
  }

  class LastInitializer extends Initializer[Integer] {
    override def apply() = 0
  }

  class LastAggregator extends Aggregator[String, Integer, Integer] {
    override def apply(k: String, v: Integer, t: Integer): Integer = v
  }

  class AddJoiner extends ValueJoiner[Integer, Integer, Integer] {
    override def apply(v1: Integer, v2: Integer): Integer = v1 + v2
  }

  class SubJoiner extends ValueJoiner[Integer, Integer, Integer] {
    override def apply(v1: Integer, v2: Integer): Integer = v1 - v2
  }

  object Fixtures {

    object Uppercase {
      val input = Seq(("x", "v1"), ("y", "v2"))
      val expected = Seq(("x", "V1"), ("y", "V2"))

      val strings = Serdes.String()
      val ints = Serdes.Integer()

      val InputTopic = "input"
      val OutputTopic = "output"

      def topology(builder: KStreamBuilder) = {
        builder.stream(strings, strings, InputTopic)
          .map((k, v) => new KeyValue(k, v.toUpperCase))
          .to(strings, strings, OutputTopic)
      }
    }

    object Multi {

      def int(i: Int) = Integer.valueOf(i)

      val inputA = Seq(("x", int(1)), ("y", int(2)))
      val inputB = Seq(("x", int(4)), ("y", int(3)))
      val expectedA = Seq(("x", int(5)), ("y", int(5)))
      val expectedB = Seq(("x", int(3)), ("y", int(1)))

      val strings = Serdes.String()
      val ints = Serdes.Integer()

      val InputATopic = "inputA"
      val InputBTopic = "inputB"
      val OutputATopic = "outputA"
      val OutputBTopic = "outputB"
      val StoreName = "store"

      def topology1Output(builder: KStreamBuilder) = {
        val streamA = builder.stream(strings, ints, InputATopic)
        val streamB = builder.stream(strings, ints, InputBTopic)

        val table = streamA.groupByKey(strings, ints).aggregate(
          new LastInitializer,
          new LastAggregator, ints, StoreName)

        streamB.leftJoin(table, new AddJoiner(), strings, ints)
          .to(strings, ints, OutputATopic)
      }

      def topology2Output(builder: KStreamBuilder) = {
        val streamA = builder.stream(strings, ints, InputATopic)
        val streamB = builder.stream(strings, ints, InputBTopic)

        val table = streamA.groupByKey(strings, ints).aggregate(
          new LastInitializer,
          new LastAggregator,
          ints,
          StoreName)

        streamB.leftJoin(table, new AddJoiner(), strings, ints)
          .to(strings, ints, OutputATopic)

        streamB.leftJoin(table, new SubJoiner(), strings, ints)
          .to(strings, ints, OutputBTopic)
      }
    }

  }

}


