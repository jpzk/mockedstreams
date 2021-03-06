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

import java.time.{Duration, Instant}
import java.util.{Properties, UUID}

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.apache.kafka.streams.{TestInputTopic, TestOutputTopic}
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.{
  StreamsConfig,
  Topology,
  TopologyTestDriver => Driver
}

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

object MockedStreams {

  def apply() = Builder()

  sealed trait Input
  case class Record(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]])
      extends Input

  implicit def recordsInstant[K, V](list: Seq[(K, V, Instant)]) =
    RecordsInstant(list)
  implicit def recordsLong[K, V](list: Seq[(K, V, Long)]) = RecordsLong(list)

  case class RecordsInstant[K, V](seq: Seq[(K, V, Instant)])
  case class RecordsLong[K, V](seq: Seq[(K, V, Long)])

  case class Builder(
      configuration: Properties = new Properties(),
      driver: Option[Driver] = None,
      stateStores: Seq[String] = Seq()
  ) {

    def config(configuration: Properties): Builder =
      this.copy(configuration = configuration)

    def topology(func: StreamsBuilder => Unit): Builder = {
      val builder = new StreamsBuilder()
      func(builder)
      val topology = builder.build()
      withTopology(() => topology)
    }

    def withTopology(t: () => Topology): Builder = {
      val props = new Properties
      props.put(
        StreamsConfig.APPLICATION_ID_CONFIG,
        s"mocked-${UUID.randomUUID().toString}"
      )
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      configuration.asScala.foreach { case (k, v) => props.put(k, v) }

      this.copy(
        driver = Some(
          new Driver(t(), props)
        )
      )
    }

    def withDriver[A](f: Driver => A) = driver match {
      case Some(d) => f(d)
      case None    => throw new TopologyNotSet
    }

    def stores(stores: Seq[String]): Builder = this.copy(stateStores = stores)

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def input[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        records: Seq[(K, V)]
    ): Builder =
      _input(topic, key, value, Left(records))

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def inputWithTime[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        records: RecordsLong[K, V]
    ): Builder = _input[K, V](topic, key, value, Right(records.seq))

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def inputWithTime[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        records: RecordsInstant[K, V]
    ): Builder =
      _input(topic, key, value, Right(records.seq.map {
        case (k, v, t) => (k, v, t.toEpochMilli())
      }))

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def output[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V]
    ): Seq[(K, V)] = withDriver { driver =>
      val testTopic = driver
        .createOutputTopic(topic, key.deserializer(), value.deserializer())
      testTopic
        .readRecordsToList()
        .asScala
        .map { tr =>
          (tr.getKey(), tr.getValue())
        }
        .toSeq
    }

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def outputTable[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V]
    ): Map[K, V] =
      output[K, V](topic, key, value).toMap


    /**
      * @throws TopologyNotSet if called before setting topology
      * @deprecated Use without size argument instead
      */    
    def output[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        size: Int 
    ): Seq[(K, V)] = output(topic, key, value)

    /**
      * @throws TopologyNotSet if called before setting topology
      * @deprecated Use without size argument instead
      */
    def outputTable[K, V](
        topic: String,
        key: Serde[K],
        value: Serde[V],
        size: Int
    ): Map[K, V] = output(topic, key, value).toMap

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def stateTable(name: String): Map[Nothing, Nothing] = withDriver { driver =>
      val records = driver.getKeyValueStore(name).all()
      val list = records.asScala.toList.map { record =>
        (record.key, record.value)
      }
      records.close()
      list.toMap
    }

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def windowStateTable[K, V](
        name: String,
        key: K,
        timeFrom: Long = 0,
        timeTo: Long = Long.MaxValue
    ): Map[java.lang.Long, ValueAndTimestamp[V]] =
      windowStateTable[K, V](
        name,
        key,
        Instant.ofEpochMilli(timeFrom),
        Instant.ofEpochMilli(timeTo)
      )

    /**
      * @throws TopologyNotSet if called before setting topology
      */
    def windowStateTable[K, V](
        name: String,
        key: K,
        timeFrom: Instant,
        timeTo: Instant
    ): Map[java.lang.Long, ValueAndTimestamp[V]] = withDriver { driver =>
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
    ) = withDriver { driver =>
      val testTopic =
        driver.createInputTopic(topic, key.serializer, value.serializer)
      val updatedRecords = records match {
        case Left(withoutTime) =>
          withoutTime.foreach {
            case (k, v) => testTopic.pipeInput(k, v)
          }
        case Right(withTime) =>
          withTime.foreach {
            case (k, v, timestamp) =>
              testTopic.pipeInput(k, v, timestamp)
          }
      }
      this
    }
  }

  class TopologyNotSet
      extends IllegalArgumentException(
        "Call a topology method before inputs, outputs and state store methods. Changed in Mocked Streams >= 3.6.0"
      )
      
  class NoTopologySpecified
      extends IllegalArgumentException(
        "No topology specified. Call topology() on builder."
      )
}
