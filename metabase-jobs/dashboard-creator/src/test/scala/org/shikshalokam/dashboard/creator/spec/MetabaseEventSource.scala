package org.shikshalokam.observation.dashboard.creator.spec

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.shikshalokam.job.dashboard.creator.domain.Event
import org.shikshalokam.job.util.JSONUtil
import org.shikshalokam.observation.dashboard.creator.fixture.EventsMock


class MetabaseEventSource extends SourceFunction[Event] {

  override def run(ctx: SourceContext[Event]): Unit = {
    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.METABASE_DASHBOARD_EVENT_1), 0, 0))
  }

  override def cancel(): Unit = {}


}
