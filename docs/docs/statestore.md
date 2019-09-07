---
layout: docs 
title:  "State Stores"
---

## State Stores

When you define your state stores via .stores(stores: Seq[String]) since 1.2, you are able to verify the state store content via the .stateTable(name: String) method:  

    import com.madewithtea.mockedstreams.MockedStreams

     val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("in-a", strings, ints, inputA)
      .input("in-b", strings, ints, inputB)
      .stores(Seq("store-name"))

     mstreams.stateTable("store-name") shouldEqual Map('a' -> 1) 