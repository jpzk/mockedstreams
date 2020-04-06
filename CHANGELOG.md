# Changelog

## Mocked Streams 3.6

* Changed internal code to interface with new 2.4 TopologyTestDriver methods (https://issues.apache.org/jira/browse/KAFKA-8233,https://cwiki.apache.org/confluence/display/KAFKA/KIP-470%3A+TopologyTestDriver+test+input+and+output+usability+improvements)
* Changed deprecated (from => 2.4.0) calls to Kafka Streams API (https://issues.apache.org/jira/browse/KAFKA-7277) 
* Added input timestamps can be specified in Long and in Instant (https://issues.apache.org/jira/browse/KAFKA-7277)
* Removed expected size in new .output and .outputTable methods
* Deprecated output methods with size parameters (not needed anymore)

## Mocked Streams 3.5

* Added support for Apache 2.4.0
* Changed Scala to 2.12.10
* Updated sbt-mdoc to 2.0.2

## Mocked Streams 3.4

* Added support for Apache 2.3.0
* Dropped support for Scala 2.11

## Mocked Streams 3.3

* Added support for Apache Kafka 2.2.0

## Mocked Streams 3.2

* Added support for Apache Kafka 2.1.1
* Thanks Matteo Gazzetta

## Mocked Streams 3.1

* Added support for Scala DSL in topology method 
* Java DSL is deprecated in topology method
* Thanks Michal Dziemianko for migrating to Scala DSL 

## Mocked Streams 2.2

* Added compatibility with Apache Kafka 2.1 
* Added Mateusz Owczarek to contributors
* Thanks Mateusz for the PR to make it Apache Kafka 2.1 compatible

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

