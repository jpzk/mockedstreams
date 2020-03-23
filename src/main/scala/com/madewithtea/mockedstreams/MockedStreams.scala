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

import java.time.Instant
import java.util.{Properties, UUID}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.{
  StreamsConfig,
  Topology,
  TopologyTestDriver => Driver
}

import scala.collection.JavaConverters._
import scala.collection.immutable

object MockedStreams {

  def apply() = Builder()

  sealed trait StreamsInput
  case class Record(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]) extends StreamsInput
  case class WallclockTime(time: Long) extends StreamsInput

  case class Builder(
      topology: Option[() => Topology] = None,
      configuration: Properties = new Properties(),
      stateStores: Seq[String] = Seq(),
      inputs: List[StreamsInput] = List.empty
  ) {

    def config(configuration: Properties): Builder =
      this.copy(configuration = configuration)

    def topology(func: StreamsBuilder => Unit): Builder = {
      val buildTopology = () => {
        val builder = new StreamsBuilder()
        func(builder)
        builder.build()
      }
      this.copy(topology = Some(buildTopology))
    }

    def withTopology(t: () => Topology): Builder = this.copy(topology = Some(t))

    def stores(stores: Seq[String]): Builder = this.copy(stateStores = stores)

    def input[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        records: Seq[(K, V)]
    ): Builder =
      _input(topic, key, value, Left(records))

    def inputWithTime[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        records: Seq[(K, V, Long)]
    ): Builder =
      _input(topic, key, value, Right(records))

    def output[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        size: Int
    ): immutable.IndexedSeq[(K, V)] = {
      if (size <= 0) throw new ExpectedOutputIsEmpty
      withProcessedDriver { driver =>
        (0 until size).flatMap { _ =>
          Option(driver.readOutput(topic, key.deserializer, value.deserializer))
          .map(r => (r.key, r.value))
        }
      }
    }

    def outputTable[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        size: Int
    ): Map[K, V] =
      output[K, V](topic, key, value, size).toMap

    def stateTable(name: String): Map[Nothing, Nothing] = withProcessedDriver {
      driver =>
        val records = driver.getKeyValueStore(name).all()
        val list = records.asScala.toList.map { record =>
          (record.key, record.value)
        }
        records.close()
        list.toMap
    }

    /**
      * @throws IllegalArgumentException if duration is negative or can't be represented as long milliseconds
      */
    def windowStateTable[K, V](
        name: String,
        key: K,
        timeFrom: Long = 0,
        timeTo: Long = Long.MaxValue
    ): Map[java.lang.Long, ValueAndTimestamp[V]] = {
      windowStateTable[K, V](
        name,
        key,
        Instant.ofEpochMilli(timeFrom),
        Instant.ofEpochMilli(timeTo)
      )
    }

    /**
      * @throws IllegalArgumentException if duration is negative or can't be represented as long milliseconds
      */
    def windowStateTable[K, V](
        name: String,
        key: K,
        timeFrom: Instant,
        timeTo: Instant
    ): Map[java.lang.Long, ValueAndTimestamp[V]] =
      withProcessedDriver { driver =>
        val store = driver.getTimestampedWindowStore[K, V](name)
        val records = store.fetch(key, timeFrom, timeTo)
        val list = records.asScala.toList.map { record =>
          (record.key, record.value)
        }
        records.close()
        list.toMap
      }

    private def _input[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        records: Either[Seq[(K, V)], Seq[(K, V, Long)]]
    ) = {
      val keySer = key.serializer
      val valSer = value.serializer
      val factory = new ConsumerRecordFactory[K, V](keySer, valSer)

      val updatedRecords = records match {
        case Left(withoutTime) =>
          withoutTime.foldLeft(inputs) {
            case (events, (k, v)) => events :+ Record(factory.create(topic, k, v))
          }
        case Right(withTime) =>
          withTime.foldLeft(inputs) {
            case (events, (k, v, timestamp)) =>
              events :+ Record(factory.create(topic, k, v, timestamp))
          }
      }
      this.copy(inputs = updatedRecords)
    }

    // state store is temporarily created in ProcessorTopologyTestDriver
    private def stream = {
      val props = new Properties
      props.put(
        StreamsConfig.APPLICATION_ID_CONFIG,
        s"mocked-${UUID.randomUUID().toString}"
      )
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      configuration.asScala.foreach { case (k, v) => props.put(k, v) }
      new Driver(topology.getOrElse(throw new NoTopologySpecified)(), props)
    }

    def advanceWallClock(time: Long): Builder = {
      this.copy(inputs = this.inputs :+ WallclockTime(time))
    }

    private def produce(driver: Driver): Unit =
      inputs.foreach {
        case Record(record) => driver.pipeInput(record)
        case WallclockTime(time) => driver.advanceWallClockTime(time)
      }

    private def withProcessedDriver[T](f: Driver => T): T = {
      if (inputs.isEmpty) throw new NoInputSpecified

      val driver = stream
      produce(driver)
      val result: T = f(driver)
      driver.close()
      result
    }
  }

  class NoTopologySpecified
      extends Exception("No topology specified. Call topology() on builder.")

  class NoInputSpecified
      extends Exception(
        "No input fixtures specified. Call input() method on builder."
      )

  class ExpectedOutputIsEmpty
      extends Exception("Output size needs to be greater than 0.")

}
