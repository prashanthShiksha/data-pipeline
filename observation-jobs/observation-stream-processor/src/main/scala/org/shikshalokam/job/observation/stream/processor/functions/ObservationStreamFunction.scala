package org.shikshalokam.job.observation.stream.processor.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.shikshalokam.job.observation.stream.processor.answers.ObservationStreamConfig
import org.shikshalokam.job.observation.stream.processor.domain.Event
import org.shikshalokam.job.util.{PostgresUtil, ScalaJsonUtil}
import org.shikshalokam.job.{BaseProcessFunction, Metrics}
import org.slf4j.LoggerFactory

import scala.collection.immutable._
import scala.collection.mutable

class ObservationStreamFunction(config: ObservationStreamConfig)(implicit val mapTypeInfo: TypeInformation[Event], @transient var postgresUtil: PostgresUtil = null)
  extends BaseProcessFunction[Event, Event](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[ObservationStreamFunction])

  override def metricsList(): List[String] = {
    List(config.observationCleanupHit, config.skipCount, config.successCount, config.totalEventsCount)
  }

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    val pgHost: String = config.pgHost
    val pgPort: String = config.pgPort
    val pgUsername: String = config.pgUsername
    val pgPassword: String = config.pgPassword
    val pgDataBase: String = config.pgDataBase
    val connectionUrl: String = s"jdbc:postgresql://$pgHost:$pgPort/$pgDataBase"
    postgresUtil = new PostgresUtil(connectionUrl, pgUsername, pgPassword)
  }

  override def close(): Unit = {
    super.close()
  }

  override def processElement(event: Event, context: ProcessFunction[Event, Event]#Context, metrics: Metrics): Unit = {

    println(s"***************** Start of Processing the Observation Event with Id = ${event._id} *****************")

    println("\n==> Solutions data ")
    println("solutionId = " + event.solutionId)
    println("submissionId = " + event._id)
    println("userId = " + event.createdBy)
    println("submissionNumber = " + event.submissionNumber)
    println("stateName = " + event.stateName)
    println("districtName = " + event.districtName)
    println("blockName = " + event.blockName)
    println("clusterName = " + event.clusterName)
    println("schoolName = " + event.schoolName)
//    println("recordType = " + event.recordType)
//    println("domainName = " + event.domain)
//    println("subdomainName = " + event.subdomain)
    println("criteriaName = " + event.criteria)
//    println("recordValue = " + event.recordValue)
//    println("maxScore = " + event.maxScore)
//    println("scoresAchived = " + event.scoresAchived)


    postgresUtil.createTable(config.createDomainsTable, config.domains)
    postgresUtil.createTable(config.createQuestionsTable, config.questions)


    def extractDomainWithCriteriaJson(event: Event): Option[String] = {
      val logger = LoggerFactory.getLogger("extractDomainWithCriteriaJson")

      try {
        val themes = Option(event.themes)

        val domainsData = themes match {
          case Some(domainList) if domainList.nonEmpty =>
            val domainResult = mutable.LinkedHashMap[String, Map[String, Any]]()

            domainList.zipWithIndex.foreach { case (domainMap, idx) =>
              val domainName = domainMap.getOrElse("name", "").toString
              val domainLevel = domainMap.getOrElse("level", "").toString

              val criteriaList = domainMap.get("criteria") match {
                case Some(critList: List[Map[String, Any]] @unchecked) => critList
                case _ => List.empty
              }

              val criteriaMap = criteriaList.map { crit =>
                val critName = crit.getOrElse("name", "").toString
                val critLevel = crit.getOrElse("level", "").toString
                critName -> Map("level" -> critLevel)
              }.toMap

              domainResult += s"Domain ${idx + 1}" -> Map(
                "subdomain" -> null,
                "level" -> domainLevel,
                "criteria" -> criteriaMap
              )
            }

            Some(ScalaJsonUtil.serialize(domainResult.toMap))

          case _ =>
            logger.warn("No 'themes' found or malformed")
            None
        }

        domainsData
      } catch {
        case e: Exception =>
          logger.error("Failed to extract domain/criteria JSON", e)
          None
      }
    }

  }


}
