---
layout: docs 
title:  "Multiple Inputs and Outputs"
---

## Multiple Input / Output Example and State

It also allows you to have multiple input and output streams. If your topology uses state stores you need to define them using .stores(stores: Seq[String]):

    import com.madewithtea.mockedstreams.MockedStreams

    val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

    mstreams.output("out-a", strings, ints, expA.size) shouldEqual(expectedA)
    mstreams.output("out-b", strings, ints, expB.size) shouldEqual(expectedB)