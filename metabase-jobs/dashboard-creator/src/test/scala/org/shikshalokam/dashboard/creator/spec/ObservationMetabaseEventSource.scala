package org.shikshalokam.dashboard.creator.spec

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.shikshalokam.dashboard.creator.fixture.ObservationEventMock
import org.shikshalokam.job.dashboard.creator.domain.observationEvent
import org.shikshalokam.job.util.JSONUtil

class ObservationMetabaseEventSource extends SourceFunction[observationEvent] {
  override def run(ctx: SourceContext[observationEvent]): Unit = {
    ctx.collect(new observationEvent(JSONUtil.deserialize[java.util.Map[String, Any]](ObservationEventMock.OBSERVATION_METABASE_DASHBOARD_EVENT_1), 0, 0))
  }

  override def cancel(): Unit = {}
}
