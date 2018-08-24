# Changelog

## Mocked Streams 2.1

* Added .inputWithTime to allow custom timestamps 
* Thanks to Dan Hamilton for .inputWithTime implementation
* Added Dan Hamilton to CONTRIBUTORS.md

## Mocked Streams 2.0

* Build against Apache Kafka 2.0
* Changes to support Kafka 2.0
* Replaced ProcessorTopologyTestDriver with TopologyTestDriver
* Removed Record class to use ConsumerRecord directly
* Added Michal Dziemianko to CONTRIBUTORS.md
* Thanks to Michal for Kafka 2.0 support

## Mocked Streams 1.8.0

* Bumping versions of dependencies
* Build against Apache Kafka 1.1.1

## Mocked Streams 1.7.0

* Bumping versions of dependencies
* Build against Apache Kafka 1.1.0
* Daniel Wojda added new way of supplying topologies (withTopology)
* Added Daniel Wojda to CONTRIBUTORS.md

## Mocked Streams 1.6.0

* Build against Apache Kafka 1.0.1
* Updated Scala 2.12 version to 2.12.4

## Mocked Streams 1.5.0

* Build against Apache Kafka 1.0
* Updated ScalaTest version to 3.0.4
* Updated RocksDB version to 5.7.3
* KStreamBuilder -> StreamsBuilder
* Updated stream creation with Consumed.with

## Mocked Streams 1.4.0

* Build against Apache Kafka 0.11.0.1
* Added record order and multiple emissions by Svend Vanderveken
* Updated SBT to 1.0.2
* Added Svend Vanderveken to CONTRIBUTORS.md

## Mocked Streams 1.2.2

* Build against Apache Kafka 0.11.0.0

## Mocked Streams 1.2.1

* Build against Apache Kafka 0.10.2.1
* Added calling of clean up method after driver run 
* Updated ScalaTest version to 3.0.2
* Updated Scala Versions 
* Added CodeCov and SCoverage coverage report

## Mocked Streams 1.2.0

* Build against Apache Kafka 0.10.2
* Added support for Scala 2.12.1
* Added .stateTable and .windowStateTable method for retrieving the content of the state stores as Map
* Added contributors file
* Removed dependencies to Log4j and Slf4j
* Updated RocksDB version to 5.0.1
* Updated ScalaTest version to 3.0.1
* Added more assertions in the test for input validation 

## Mocked Streams 1.1.0 

* Build against Apache Kafka 0.10.1.1

## Mocked Streams 1.0.0

* Support for Scala 2.11.8
* Support for Apache Kafka 0.10.1.0
* Released on Maven Central Repository
* Multiple input and output streams
* Specification of topology as a function: (KStreamBuilder => Unit)
* Added .config method to pass configuration to Kafka Streams instance
* Added .outputTable method will return compacted table as Map[K,V]
* Test suite

