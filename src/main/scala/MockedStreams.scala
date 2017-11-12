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

import java.util.{Properties, UUID}

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig}
import org.apache.kafka.streams.state.ReadOnlyWindowStore
import org.apache.kafka.test.{ProcessorTopologyTestDriver => Driver}

import scala.collection.JavaConverters._

object MockedStreams {

  def apply() = Builder()

  case class Record(topic: String, key: Array[Byte], value: Array[Byte])

  case class Builder(topology: Option[(StreamsBuilder => Unit)] = None,
                     configuration: Properties = new Properties(),
                     stateStores: Seq[String] = Seq(),
                     inputs: List[Record] = List.empty) {

    def config(configuration: Properties) = this.copy(configuration = configuration)

    def topology(func: (StreamsBuilder => Unit)) = this.copy(topology = Some(func))

    def stores(stores: Seq[String]) = this.copy(stateStores = stores)

    def input[K, V](topic: String, key: Serde[K], value: Serde[V], newRecords: Seq[(K, V)]) = {
      val keySer = key.serializer
      val valSer = value.serializer

      val updatedRecords = newRecords.foldLeft(inputs) {
        case (events, (k, v)) =>
          val newRecord = Record(topic, keySer.serialize(topic, k), valSer.serialize(topic, v))
          events :+ newRecord 
      }

      this.copy(inputs = updatedRecords)
    }

    def output[K, V](topic: String, key: Serde[K], value: Serde[V], size: Int) = {
      if (size <= 0) throw new ExpectedOutputIsEmpty
      withProcessedDriver { driver =>
        (0 until size).flatMap { i =>
          Option(driver.readOutput(topic, key.deserializer, value.deserializer)) match {
            case Some(record) => Some((record.key, record.value))
            case None => None
          }
        }
      }
    }

    def outputTable[K, V](topic: String, key: Serde[K], value: Serde[V], size: Int): Map[K, V] =
      output[K, V](topic, key, value, size).toMap

    def stateTable(name: String): Map[Nothing, Nothing] = withProcessedDriver { driver =>
      val records = driver.getKeyValueStore(name).all()
      val list = records.asScala.toList.map { record => (record.key, record.value) }
      records.close()
      list.toMap
    }

    def windowStateTable[K, V](name: String, key: K, timeFrom: Long = 0,
                               timeTo: Long = Long.MaxValue) = withProcessedDriver { driver =>
      val store = driver.getStateStore(name).asInstanceOf[ReadOnlyWindowStore[K, V]]
      val records = store.fetch(key, timeFrom, timeTo)
      val list = records.asScala.toList.map { record => (record.key, record.value) }
      records.close()
      list.toMap
    }

    // state store is temporarily created in ProcessorTopologyTestDriver
    private def stream = {
      val props = new Properties
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, s"mocked-${UUID.randomUUID().toString}")
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      props.putAll(configuration)

      val builder = new StreamsBuilder()

      topology match {
        case Some(t) => t(builder)
        case _ => throw new NoTopologySpecified
      }

      new Driver(new StreamsConfig(props), builder.build())
    }

    private def produce(driver: Driver): Unit = {
      inputs.foreach{
        case Record(topic, key, value) =>
          driver.process(topic, key, value)
      }
    }

    private def withProcessedDriver[T](f: Driver => T): T = {
      if(inputs.isEmpty)
        throw new NoInputSpecified

      val driver = stream
      produce(driver)
      val result: T = f(driver)
      driver.close
      result
    }
  }

  class NoTopologySpecified extends Exception("No topology specified. Call topology() on builder.")

  class NoInputSpecified extends Exception("No input fixtures specified. Call input() method on builder.")

  class ExpectedOutputIsEmpty extends Exception("Output size needs to be greater than 0.")
}
