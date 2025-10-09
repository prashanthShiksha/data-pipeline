package org.shikshalokam.mentoring.dashboard.creator.spec

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.shikshalokam.job.mentoring.dashboard.creator.domain.Event
import org.shikshalokam.job.util.JSONUtil
import org.shikshalokam.mentoring.dashboard.creator.fixture.EventsMock

class MentoringMetabaseEventSource extends SourceFunction[Event] {

  override def run(ctx: SourceContext[Event]): Unit = {
    ctx.collect(new Event(JSONUtil.deserialize[java.util.Map[String, Any]](EventsMock.EVENT1), 0, 0))
  }

  override def cancel(): Unit = {}


}
