package org.shikshalokam.dashboard.creator.spec

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import java.util


class GenerateObservationMetabaseDashboardSink extends SinkFunction[String] {

  override def invoke(value: String): Unit = {
    synchronized {
      println(value)
      GenerateMetabaseDashboardSink.values.add(value)
    }
  }
}

object GenerateObservationMetabaseDashboardSink {
  val values: util.List[String] = new util.ArrayList()
}