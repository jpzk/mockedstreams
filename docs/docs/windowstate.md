---
layout: docs 
title:  "Windowed State Stores"
---

## Window State Store 

When you define your state stores via .stores(stores: Seq[String]) since 1.2 and added the timestamp extractor to the config, you are able to verify the window state store content via the .windowStateTable(name: String, key: K) method:  

    import com.madewithtea.mockedstreams.MockedStreams

    val props = new Properties
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
      classOf[TimestampExtractors.CustomTimestampExtractor].getName)

    val mstreams = MockedStreams()
      .topology { builder => builder.stream(...) [...] } // Scala DSL
      .input("in-a", strings, ints, inputA)
      .stores(Seq("store-name"))
      .config(props)

    mstreams.windowStateTable("store-name", "x") shouldEqual someMapX
    mstreams.windowStateTable("store-name", "y") shouldEqual someMapY
