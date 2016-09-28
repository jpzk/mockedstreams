/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package mwt.mockedstreams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.{Aggregator, Initializer, KStreamBuilder, ValueJoiner}
import org.scalatest.{FlatSpec, Matchers}

class MockedStreamsSpec extends FlatSpec with Matchers {

  behavior of "MockedStreams"

  def int(i: Int) = Integer.valueOf(i)

  object Fixtures {

    object Uppercase {
      val input = Seq(("x", "v1"), ("y", "v2"))
      val expected = Seq(("x", "V1"), ("y", "V2"))

      val strings = Serdes.String()
      val ints = Serdes.Integer()
    }

    object Multi {
      val inputA = Seq(("x", int(1)), ("y", int(2)))
      val inputB = Seq(("x", int(4)), ("y", int(3)))
      val expectedA = Seq(("x", int(5)), ("y", int(5)))
      val expectedB = Seq(("x", int(3)), ("y", int(1)))

      val strings = Serdes.String()
      val ints = Serdes.Integer()
    }
  }

  it should "throw exception when expected size is <= 0" in {
    import Fixtures.Uppercase._
    import MockedStreams.ExpectedOutputIsEmpty

    val topology = new UppercaseTopologyString()

    an[ExpectedOutputIsEmpty] should be thrownBy
      MockedStreams()
        .topology(topology)
        .input(topology.Input, strings, strings, input)
        .output(topology.Output, strings, strings, 0)

    an[ExpectedOutputIsEmpty] should be thrownBy
      MockedStreams()
        .topology(topology)
        .input(topology.Input, strings, strings, input)
        .output(topology.Output, strings, strings, -1)
  }

  it should "throw exception when no input specified" in {
    import Fixtures.Uppercase._
    import MockedStreams.NoInputSpecified
    val topology = new UppercaseTopologyString()

    an[NoInputSpecified] should be thrownBy
      MockedStreams()
        .topology(topology)
        .output(topology.Output, strings, strings, expected.size)
  }

  it should "assert correctly when processing strings to uppercase" in {
    import Fixtures.Uppercase._

    val topology = new UppercaseTopologyString()

    val output = MockedStreams()
      .topology(topology)
      .input(topology.Input, strings, strings, input)
      .output(topology.Output, strings, strings, expected.size)

    output shouldEqual expected
  }

  it should "assert correctly when processing multi input topology" in {
    import Fixtures.Multi._

    val topology = new MultiInputTopology()

    val output = MockedStreams()
      .topology(topology)
      .input[String, Integer](topology.InputA, strings, ints, inputA)
      .input[String, Integer](topology.InputB, strings, ints, inputB)
      .stateStores(Seq(topology.Store))
      .output(topology.Output, strings, ints, expectedA.size)

    output shouldEqual expectedA
  }

  it should "assert correctly when processing multi input output topology" in {
    import Fixtures.Multi._

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


  class UppercaseTopologyString extends MockedTopology {
    import Fixtures.Uppercase._

    val Input = "input"
    val Output = "output"

    override def builtBy(builder: KStreamBuilder): Unit = {
      builder.stream(strings, strings, Input)
        .map((k, v) => new KeyValue(k, v.toUpperCase))
        .to(strings, strings, Output)
    }
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

  class MultiInputTopology extends MockedTopology {
    import Fixtures.Multi._

    val InputA = "inputA"
    val InputB = "inputB"
    val Output = "ouput"
    val Store = "store"

    override def builtBy(builder: KStreamBuilder): Unit = {
      val streamA = builder.stream(strings, ints, InputA)
      val streamB = builder.stream(strings, ints, InputB)

      val table = streamA.groupByKey(strings, ints).aggregate(
        new LastInitializer,
        new LastAggregator, ints, Store)

      streamB.leftJoin(table, new AddJoiner(), strings, ints)
        .to(strings, ints, Output)
    }
  }

  class MultiInputOutputTopology extends MockedTopology {
    import Fixtures.Multi._

    val InputA = "inputA"
    val InputB = "inputB"
    val OutputA = "outputA"
    val OutputB = "outputB"
    val Store = "store"

    override def builtBy(builder: KStreamBuilder): Unit = {
      val streamA = builder.stream(strings, ints, InputA)
      val streamB = builder.stream(strings, ints, InputB)

      val table = streamA.groupByKey(strings, ints).aggregate(
        new LastInitializer,
        new LastAggregator,
        ints,
        Store)

      streamB.leftJoin(table, new AddJoiner(), strings, ints)
        .to(strings, ints, OutputA)

      streamB.leftJoin(table, new SubJoiner(), strings, ints)
        .to(strings, ints, OutputB)
    }
  }

}


