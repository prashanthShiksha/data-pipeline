package org.shikshalokam.job.dashboard.creator.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.shikshalokam.job.dashboard.creator.domain.observationEvent
import org.shikshalokam.job.dashboard.creator.task.ObservationMetabaseDashboardConfig
import org.shikshalokam.job.util.{MetabaseUtil, PostgresUtil}
import org.shikshalokam.job.{BaseProcessFunction, Metrics}
import org.slf4j.LoggerFactory

import scala.collection.immutable._


class ObservationMetabaseDashboardFunction(config: ObservationMetabaseDashboardConfig)(implicit val mapTypeInfo: TypeInformation[observationEvent], @transient var postgresUtil: PostgresUtil = null, @transient var metabaseUtil: MetabaseUtil = null)
  extends BaseProcessFunction[observationEvent, observationEvent](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[ObservationMetabaseDashboardFunction])

  override def metricsList(): List[String] = {
    List(config.observationMetabaseDashboardCleanupHit, config.skipCount, config.successCount, config.totalEventsCount)
  }

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    val pgHost: String = config.pgHost
    val pgPort: String = config.pgPort
    val pgUsername: String = config.pgUsername
    val pgPassword: String = config.pgPassword
    val pgDataBase: String = config.pgDataBase
    val metabaseUrl: String = config.metabaseUrl
    val metabaseUsername: String = config.metabaseUsername
    val metabasePassword: String = config.metabasePassword
    val connectionUrl: String = s"jdbc:postgresql://$pgHost:$pgPort/$pgDataBase"
    postgresUtil = new PostgresUtil(connectionUrl, pgUsername, pgPassword)
    metabaseUtil = new MetabaseUtil(metabaseUrl, metabaseUsername, metabasePassword)
  }

  override def close(): Unit = {
    super.close()
  }

  override def processElement(event: observationEvent, context: ProcessFunction[observationEvent, observationEvent]#Context, metrics: Metrics): Unit = {

    println(s"***************** Start of Processing the Metabase Dashboard Event with Id = ${event._id}*****************")

    val solution_id = event.solution_id
    val domainTable = s"${solution_id}_domain"
    println(s"domainTable: $domainTable")
    val questionTable = s"${solution_id}_questions"
    println(s"questionTable: $questionTable")
    val chartType = event.chartType
    println(s"chartType: $chartType")
    val metaDataTable = config.dashboard_metadata
    val report_config: String = config.report_config
    val metabaseDatabase: String = config.metabaseDatabase

    event.reportType match {
      case "Observation" =>
        chartType.asInstanceOf[List[String]].foreach {
          case ("domain") =>
            println("Executing domain part...")
            try {
              val collectionName: String = s"Observation Domain Collection[$solution_id]"
              val dashboardName: String = s"Observation Domain Report[$solution_id]"
              val groupName: String = s"Report_Admin"
              val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE entity_id = '$solution_id';"
              val collectionId: Int = CreateDashboard.checkAndCreateCollection(collectionName, s"${solution_id}_domain Report", metabaseUtil, postgresUtil, createDashboardQuery)
              val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, dashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
              val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
              val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, domainTable, "state_name", postgresUtil, createDashboardQuery)
              val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, domainTable, "district_name", postgresUtil, createDashboardQuery)
              val schoolnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, domainTable, "school_name", postgresUtil, createDashboardQuery)
              val reportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Domain' AND report_name = 'Domain-Report';"
              val questionCardIdList = UpdateDomainJsonFiles.ProcessAndUpdateJsonFiles(reportConfigQuery, collectionId, databaseId, dashboardId, statenameId, districtnameId, schoolnameId, domainTable, metabaseUtil, postgresUtil, report_config)
              val questionIdsString = "[" + questionCardIdList.mkString(",") + "]"
              val parametersQuery: String = s"SELECT config FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'Domain-Parameter'"
              UpdateParameters.UpdateAdminParameterFunction(metabaseUtil, parametersQuery, dashboardId, postgresUtil)
              val updateTableQuery = s"UPDATE $metaDataTable SET  collection_id = '$collectionId', dashboard_id = '$dashboardId', question_ids = '$questionIdsString', status = 'Success', error_message = '' WHERE entity_id = '$solution_id';"
              postgresUtil.insertData(updateTableQuery)
              CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, groupName, collectionId)
            } catch {
              case e: Exception =>
                val updateTableQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}'  WHERE entity_id = '$solution_id';"
                postgresUtil.insertData(updateTableQuery)
                println(s"An error occurred: ${e.getMessage}")
                e.printStackTrace()
            }
          case ("question") =>
            println("Executing question part...")
            try {
              val collectionName: String = s"Observation Question Collection[$solution_id]"
              val dashboardName: String = s"Observation Question Report[$solution_id]"
              val groupName: String = s"Report_Admin"
              val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE entity_id = '$solution_id';"
              val collectionId: Int = CreateDashboard.checkAndCreateCollection(collectionName, s"${solution_id}_question Report", metabaseUtil, postgresUtil, createDashboardQuery)
              val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, dashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
              val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
              val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, questionTable, "state_name", postgresUtil, createDashboardQuery)
              val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, questionTable, "district_name", postgresUtil, createDashboardQuery)
              val schoolnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, questionTable, "school_name", postgresUtil, createDashboardQuery)
              val questionCardIdList = UpdateQuestionJsonFiles.ProcessAndUpdateJsonFiles(collectionId, databaseId, dashboardId, statenameId, districtnameId, schoolnameId, questionTable, metabaseUtil, postgresUtil, report_config)
              val questionIdsString = "[" + questionCardIdList.mkString(",") + "]"
              val parametersQuery: String = s"SELECT config FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'Domain-Parameter'"
              UpdateParameters.UpdateAdminParameterFunction(metabaseUtil, parametersQuery, dashboardId, postgresUtil)
              val updateTableQuery = s"UPDATE $metaDataTable SET  collection_id = '$collectionId', dashboard_id = '$dashboardId', question_ids = '$questionIdsString', status = 'Success', error_message = '' WHERE entity_id = '$solution_id';"
              postgresUtil.insertData(updateTableQuery)
              CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, groupName, collectionId)
            } catch {
              case e: Exception =>
                val updateTableQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}'  WHERE entity_id = '$solution_id';"
                postgresUtil.insertData(updateTableQuery)
                println(s"An error occurred: ${e.getMessage}")
                e.printStackTrace()
            }
        }
    }


//    event.reportType match {
//      case "Observation" =>chartType.asInstanceOf[Map[String, Any]].foreach {
//        case "domain" =>
//          println("Executing domain part...")
//          try {
//            val collectionName: String = s"Observation Domain Collection[$solution_id]"
//            val dashboardName: String = s"Observation Domain Report[$solution_id]"
//            val groupName: String = s"Report_Admin"
//            val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE entity_id = '$solution_id';"
//            val collectionId: Int = CreateDashboard.checkAndCreateCollection(collectionName, s"${solution_id}_domain Report", metabaseUtil, postgresUtil, createDashboardQuery)
//            val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, dashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
//            val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
//            val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, domainTable, "state_name", postgresUtil, createDashboardQuery)
//            val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, domainTable, "district_name", postgresUtil, createDashboardQuery)
//            val schoolnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, domainTable, "school_name", postgresUtil, createDashboardQuery)
//            val reportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Domain' AND report_name = 'Domain-Report';"
//            val questionCardIdList = UpdateDomainJsonFiles.ProcessAndUpdateJsonFiles(reportConfigQuery, collectionId, databaseId, dashboardId, statenameId, districtnameId, schoolnameId, domainTable, metabaseUtil, postgresUtil, report_config)
//            val questionIdsString = "[" + questionCardIdList.mkString(",") + "]"
//            val parametersQuery: String = s"SELECT config FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'Domain-Parameter'"
//            UpdateParameters.UpdateAdminParameterFunction(metabaseUtil, parametersQuery, dashboardId, postgresUtil)
//            val updateTableQuery = s"UPDATE $metaDataTable SET  collection_id = '$collectionId', dashboard_id = '$dashboardId', question_ids = '$questionIdsString', status = 'Success', error_message = '' WHERE entity_id = '$solution_id';"
//            postgresUtil.insertData(updateTableQuery)
//            CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, groupName, collectionId)
//          } catch {
//            case e: Exception =>
//              val updateTableQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}'  WHERE entity_id = '$solution_id';"
//              postgresUtil.insertData(updateTableQuery)
//              println(s"An error occurred: ${e.getMessage}")
//              e.printStackTrace()
//          }
//        case "question" =>
//          println("Executing question part...")
//          try {
//            val collectionName: String = s"Observation Question Collection[$solution_id]"
//            val dashboardName: String = s"Observation Question Report[$solution_id]"
//            val groupName: String = s"Report_Admin"
//            val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE entity_id = '$solution_id';"
//            val collectionId: Int = CreateDashboard.checkAndCreateCollection(collectionName, s"${solution_id}_question Report", metabaseUtil, postgresUtil, createDashboardQuery)
//            val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, dashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
//            val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
//            val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, questionTable, "state_name", postgresUtil, createDashboardQuery)
//            val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, questionTable, "district_name", postgresUtil, createDashboardQuery)
//            val schoolnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, questionTable, "school_name", postgresUtil, createDashboardQuery)
//            val questionCardIdList = UpdateQuestionJsonFiles.ProcessAndUpdateJsonFiles(collectionId, databaseId, dashboardId, statenameId, districtnameId, schoolnameId, questionTable, metabaseUtil, postgresUtil, report_config)
//            val questionIdsString = "[" + questionCardIdList.mkString(",") + "]"
//            val parametersQuery: String = s"SELECT config FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'Domain-Parameter'"
//            UpdateParameters.UpdateAdminParameterFunction(metabaseUtil, parametersQuery, dashboardId, postgresUtil)
//            val updateTableQuery = s"UPDATE $metaDataTable SET  collection_id = '$collectionId', dashboard_id = '$dashboardId', question_ids = '$questionIdsString', status = 'Success', error_message = '' WHERE entity_id = '$solution_id';"
//            postgresUtil.insertData(updateTableQuery)
//            CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, groupName, collectionId)
//          } catch {
//            case e: Exception =>
//              val updateTableQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}'  WHERE entity_id = '$solution_id';"
//              postgresUtil.insertData(updateTableQuery)
//              println(s"An error occurred: ${e.getMessage}")
//              e.printStackTrace()
//          }
//        }
      println(s"********** Completed Processing Metabase Observation Dashboard ***********")
  }
}
