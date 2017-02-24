# Changelog

## Mocked Streams 1.2.0

* Build against Apache Kafka 0.10.2
* Added support for Scala 2.12.1
* Added .stateTable method for retrieving the contant of the state store as Map
* Added contributors file
* Removed dependencies to Log4j and Slf4j
* Updated RocksDB version to 5.0.1
* Updated ScalaTest version to 3.0.1

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

