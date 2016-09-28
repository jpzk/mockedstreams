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

import java.util.{Properties, UUID}

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStreamBuilder
import org.apache.kafka.test.ProcessorTopologyTestDriver

trait MockedTopology {
  def builtBy(builder: KStreamBuilder): Unit
}

object MockedStreams {

  def apply() = Builder()

  case class Input(seq: Seq[(Array[Byte], Array[Byte])])

  case class Builder(topology: Option[MockedTopology] = None,
                     stateStoresUsed: Seq[String] = Seq(),
                     inputs: Map[String, Input] = Map()) {

    def topology(topology: MockedTopology) = this.copy(topology = Some(topology))

    def stateStores(stores: Seq[String]) = this.copy(stateStoresUsed = stores)

    def input[K, V](topic: String, keySerde: Serde[K], valSerde: Serde[V], seq: Seq[(K, V)]) = {
      val keySer = keySerde.serializer()
      val valSer = valSerde.serializer()
      val in = seq.map { case (k, v) => (keySer.serialize(topic, k), valSer.serialize(topic, v)) }
      this.copy(inputs = inputs + (topic -> Input(in)))
    }

    def output[K, V](topic: String, keySerde: Serde[K], valSerde: Serde[V], size: Int) = {
      if (size <= 0)
        throw new ExpectedOutputIsEmpty
      if (inputs.size == 0)
        throw new NoInputSpecified

      val t = topology match {
        case Some(t) => t
        case _ => throw new NoTopologySpecified
      }

      val driver = stream(t, stateStoresUsed)
      produce(driver)

      val keyDes = keySerde.deserializer()
      val valDes = valSerde.deserializer()
      (0 until size).flatMap { i =>
        Option(driver.readOutput(topic, keyDes, valDes)) match {
          case Some(record) => Some((record.key, record.value))
          case None => None
        }
      }
    }

    // state store is temporarily created in ProcessorTopologyTestDriver
    private def stream(topology: MockedTopology, stateStores: Seq[String]) = {
      val props = new Properties
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, s"mocked-${UUID.randomUUID().toString}")
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")


      val builder = new KStreamBuilder
      topology.builtBy(builder)
      new ProcessorTopologyTestDriver(new StreamsConfig(props), builder, stateStores: _*)
    }

    private def produce(driver: ProcessorTopologyTestDriver) = {
      inputs.foreach { case (topic, input) =>
        input.seq.foreach { case (key, value) =>
          driver.process(topic, key, value)
        }
      }
    }
  }

  class NoTopologySpecified extends Exception("No topology specified. Call topology() on builder.")

  class NoInputSpecified extends Exception("No input fixtures specified. Call input() method on builder.")

  class ExpectedOutputIsEmpty extends Exception("Output size needs to be greater than 0.")

}


