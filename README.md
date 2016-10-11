# Mocked Streams (preview)
[![Build Status](https://travis-ci.org/jpzk/mockedstreams.svg?branch=master)](https://travis-ci.org/jpzk/mockedstreams) [![Coverage Status](https://coveralls.io/repos/github/jpzk/mockedstreams/badge.svg?branch=master)](https://coveralls.io/github/jpzk/mockedstreams?branch=master) [![License](http://img.shields.io/:license-Apache%202-grey.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

**Mocked Streams is a Kafka Streams testing library** for Kafka >= 0.10.1-SNAPSHOT (snapshot JARs included in /lib) which makes use of the ProcessorTopologyTestDriver, therefore **no Kafka brokers and Zookeeper needed** and tests can be run in parallel. It integrates well with any testing framework. The library will be published on the Maven repositories once the new Kafka version is released.


