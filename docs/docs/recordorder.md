---
layout: docs 
title:  "Record Order"
---

## Record order and multiple emissions

The records provided to the mocked stream will be submitted to the topology during the test in the order in which they appear in the fixture. You can also submit records multiple times to the same topics, at various moments in your scenario. 

This can be handy to validate that your topology behaviour is or is not dependent on the order in which the records are received and processed. 

In the example below, 2 records are first submitted to topic A, then 3 to topic B, then 1 more to topic A again. 

    val firstInputForTopicA = Seq(("x", int(1)), ("y", int(2)))
    val firstInputForTopicB = Seq(("x", int(4)), ("y", int(3)), ("y", int(5)))
    val secondInputForTopicA = Seq(("y", int(4)))

    val expectedOutput = Seq(("x", 5), ("y", 5), ("y", 7), ("y", 9))

    val builder = MockedStreams()
      .topology(topologyTables) // Scala DSL
      .input(InputATopic, strings, ints, firstInputForTopicA)
      .input(InputBTopic, strings, ints, firstInputForTopicB)
      .input(InputATopic, strings, ints, secondInputForTopicA)