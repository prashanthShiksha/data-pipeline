package org.shikshalokam.job.dashboard.creator.task

import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.shikshalokam.job.connector.FlinkKafkaConnector
import org.shikshalokam.job.dashboard.creator.domain.observationEvent
import org.shikshalokam.job.dashboard.creator.functions.ObservationMetabaseDashboardFunction
import org.shikshalokam.job.util.FlinkUtil

import java.io.File

class ObservationMetabaseDashboardTask(config: ObservationMetabaseDashboardConfig, kafkaConnector: FlinkKafkaConnector) {
  println("inside ObservationMetabaseDashboardTask class")

  private val serialVersionUID = -7729362727131516112L

  def process(): Unit = {
    implicit val env: StreamExecutionEnvironment = FlinkUtil.getExecutionContext(config)
    implicit val eventTypeInfo: TypeInformation[observationEvent] = TypeExtractor.getForClass(classOf[observationEvent])
    val source = kafkaConnector.kafkaJobRequestSource[observationEvent](config.inputTopic)
    println(s"Kafka Source: $source")
    env.addSource(source).name(config.observationMetabaseDashboardProducer)
      .uid(config.observationMetabaseDashboardProducer).setParallelism(config.mlMetabaseParallelism).rebalance
      .process(new ObservationMetabaseDashboardFunction(config))
      .name(config.observationMetabaseDashboardFunction).uid(config.observationMetabaseDashboardFunction)
      .setParallelism(config.mlMetabaseParallelism)

    env.execute(config.jobName)
  }
}

object ObservationMetabaseDashboardTask {
  def main(args: Array[String]): Unit = {
    println("Starting up the Metabase Dashboard creation Job")
    val configFilePath = Option(ParameterTool.fromArgs(args).get("config.file.path"))
    val config = configFilePath.map {
      path => ConfigFactory.parseFile(new File(path)).resolve()
    }.getOrElse(ConfigFactory.load("metabase-dashboard.conf").withFallback(ConfigFactory.systemEnvironment()))
    val observationMetabaseDashboardConfig = new ObservationMetabaseDashboardConfig(config)
    val kafkaUtil = new FlinkKafkaConnector(observationMetabaseDashboardConfig)
    val task = new ObservationMetabaseDashboardTask(observationMetabaseDashboardConfig, kafkaUtil)
    task.process()
  }
}