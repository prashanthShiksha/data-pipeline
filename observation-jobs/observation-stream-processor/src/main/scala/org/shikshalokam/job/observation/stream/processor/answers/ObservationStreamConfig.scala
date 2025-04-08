package org.shikshalokam.job.observation.stream.processor.answers

import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.api.scala.OutputTag
import org.shikshalokam.job.observation.stream.processor.domain.Event
import org.shikshalokam.job.BaseJobConfig

import scala.collection.JavaConverters._

class ObservationStreamConfig(override val config: Config) extends BaseJobConfig(config, "ObservationsStreamJob") {

  implicit val mapTypeInfo: TypeInformation[Event] = TypeExtractor.getForClass(classOf[Event])

  // Kafka Topics Configuration
  val inputTopic: String = config.getString("kafka.input.topic")
  val outputTopic: String = config.getString("kafka.output.topic")

  // Output Tags
  val eventOutputTag: OutputTag[String] = OutputTag[String]("observation-dashboard-output-event")

  // Parallelism
  override val kafkaConsumerParallelism: Int = config.getInt("task.consumer.parallelism")
  val observationStreamParallelism: Int = config.getInt("task.sl.observations.stream.parallelism")
  val metabaseDashboardParallelism: Int = config.getInt("task.sl.metabase.observations.dashboard.parallelism")

  // Consumers
  val observationStreamConsumer: String = "observation-stream-consumer"
  val metabaseDashboardProducer = "metabase-observation-dashboard-producer"

  // Functions
  val observationStreamFunction: String = "ObservationStreamFunction"

  // Project submissions job metrics
  val observationCleanupHit = "observation-cleanup-hit"
  val skipCount = "skipped-message-count"
  val successCount = "success-message-count"
  val totalEventsCount = "total-observation-events-count"

  //report-config
  val reportsEnabled: Set[String]= config.getStringList("reports.enabled").asScala.toSet

  // PostgreSQL connection config
  val pgHost: String = config.getString("postgres.host")
  val pgPort: String = config.getString("postgres.port")
  val pgUsername: String = config.getString("postgres.username")
  val pgPassword: String = config.getString("postgres.password")
  val pgDataBase: String = config.getString("postgres.database")
  val domains: String = config.getString("postgres.tables.domainsTable")
  val questions: String = config.getString("postgres.tables.questionsTable")
  val dashboard_metadata: String = config.getString("postgres.tables.dashboardMetadataTable")

  val createDomainsTable =
    s"""CREATE TABLE IF NOT EXISTS $domains (
       |    id TEXT PRIMARY KEY,
       |    solution_id TEXT ,
       |    submission_id TEXT,
       |    user_id TEXT,
       |    submission_number TEXT,
       |    state_name TEXT,
       |    district_name TEXT,
       |    block_name TEXT,
       |    cluster_name TEXT,
       |    school_name TEXT,
       |    record_type TEXT,
       |    domain_name TEXT,
       |    subdomain_name TEXT,
       |    criteria_name TEXT,
       |    record_value TEXT,
       |    max_score TEXT,
       |    scores_achived TEXT,
       |);""".stripMargin


  val createQuestionsTable =
    s"""CREATE TABLE IF NOT EXISTS $questions (
       |    id TEXT PRIMARY KEY,
       |    solution_id TEXT ,
       |    submission_id TEXT,
       |    state_name TEXT,
       |    district_name TEXT,
       |    block_name TEXT,
       |    cluster_name TEXT,
       |    org_name TEXT,
       |    question_id TEXT,
       |    question_text TEXT,
       |    value TEXT,
       |    score TEXT,
       |    has_parent_question TEXT,
       |    parent_question_text TEXT,
       |    evidence TEXT,
       |    submitted_at TEXT,
       |    remarks TEXT,
       |    question_type TEXT
       |);""".stripMargin

}
