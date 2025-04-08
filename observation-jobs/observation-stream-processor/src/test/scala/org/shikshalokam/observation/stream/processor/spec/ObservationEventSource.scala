package org.shikshalokam.observation.stream.processor.spec

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.shikshalokam.job.observation.stream.processor.domain.Event
import org.shikshalokam.job.util.JSONUtil
import org.shikshalokam.observation.stream.processor.fixture.EventsMock

class ObservationEventSource extends SourceFunction[Event] {

  override def run(ctx: SourceContext[Event]): Unit = {
    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.DEV_EVENT_1), 0, 0))
//    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.DEV_EVENT_2), 0, 0))
//    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.QA_EVENT_1), 0, 0))
//    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.QA_EVENT_2), 0, 0))
//    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.TEST_DASHBOARD_1), 0, 0))
//    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.TEST_DASHBOARD_2), 0, 0))
//    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.TEST_DASHBOARD_3), 0, 0))
  }

  override def cancel(): Unit = {}
}
