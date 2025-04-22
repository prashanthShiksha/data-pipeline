package org.shikshalokam.job.dashboard.creator.task

import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.shikshalokam.job.BaseJobConfig
import org.shikshalokam.job.dashboard.creator.domain.observationEvent

class ObservationMetabaseDashboardConfig(override val config: Config) extends BaseJobConfig(config, "ObservationMetabaseDashboardJob") {
  println("inside ObservationMetabaseDashboardConfig class")
  implicit val mapTypeInfo: TypeInformation[observationEvent] = TypeExtractor.getForClass(classOf[observationEvent])

  // Kafka Topics Configuration
  val inputTopic: String = config.getString("kafka.observation.input.topic")

  // Parallelism
  val mlMetabaseParallelism: Int = config.getInt("task.sl.metabase.dashboard.parallelism")

  // Consumers
  val observationMetabaseDashboardProducer: String = "Observation-metabase-dashboard-consumer"

  // Functions
  val observationMetabaseDashboardFunction: String = "ObservationMetabaseDashboardFunction"

  // Metabase Dashboard submissions job metrics
  val observationMetabaseDashboardCleanupHit = "Observation-metabase-dashboard-cleanup-hit"
  val skipCount = "skipped-message-count"
  val successCount = "success-message-count"
  val totalEventsCount = "total-Observation-metabase-dashboard-events-count"

  // PostgreSQL connection config
  val pgHost: String = config.getString("postgres.host")
  val pgPort: String = config.getString("postgres.port")
  val pgUsername: String = config.getString("postgres.username")
  val pgPassword: String = config.getString("postgres.password")
  val pgDataBase: String = config.getString("postgres.database")
  val solutions: String = config.getString("postgres.tables.solutionsTable")
  val projects: String = config.getString("postgres.tables.projectsTable")
  val tasks: String = config.getString("postgres.tables.tasksTable")
  val dashboard_metadata: String = config.getString("postgres.tables.dashboardMetadataTable")
  val report_config: String = config.getString("postgres.tables.reportConfigTable")

  // Metabase connection config
  val metabaseUrl: String = config.getString("metabase.url")
  val metabaseUsername: String = config.getString("metabase.username")
  val metabasePassword: String = config.getString("metabase.password")
  val metabaseDatabase: String = config.getString("metabase.database")
}
