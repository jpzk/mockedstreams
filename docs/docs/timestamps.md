---
layout: docs 
title:  "Adding Timestamps"
---

## Adding Timestamps

With .input the input records timestamps are set to 0 default timestamp of 0. This e.g. prevents testing Join windows of Kafka streams as it cannot produce records with different timestamps. However, using .inputWithTime allows adding timestamps like in the following example: 

    val inputA = Seq(
      ("x", int(1), 1000L),
      ("x", int(1), 1001L),
      ("x", int(1), 1002L)
    )

    val builder = MockedStreams()
      .topology(topology1WindowOutput) // Scala DSL
      .inputWithTime(InputCTopic, strings, ints, inputA)
      .stores(Seq(StoreName))