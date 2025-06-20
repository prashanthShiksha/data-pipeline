package org.shikshalokam.job.dashboard.creator.functions

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.shikshalokam.job.dashboard.creator.domain.Event
import org.shikshalokam.job.dashboard.creator.miDashboard.{ComparePage, DistrictPage, HomePage, StatePage, Utils}
import org.shikshalokam.job.dashboard.creator.task.ProjectMetabaseDashboardConfig
import org.shikshalokam.job.util.{MetabaseUtil, PostgresUtil}
import org.shikshalokam.job.{BaseProcessFunction, Metrics}
import org.shikshalokam.job.util.JSONUtil.mapper

import scala.collection.JavaConverters._
import scala.collection.mutable
import org.slf4j.LoggerFactory

import scala.collection.immutable._

class ProjectMetabaseDashboardFunction(config: ProjectMetabaseDashboardConfig)(implicit val mapTypeInfo: TypeInformation[Event], @transient var postgresUtil: PostgresUtil = null, @transient var metabaseUtil: MetabaseUtil = null)
  extends BaseProcessFunction[Event, Event](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[ProjectMetabaseDashboardFunction])

  override def metricsList(): List[String] = {
    List(config.metabaseDashboardCleanupHit, config.skipCount, config.successCount, config.totalEventsCount)
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

  override def processElement(event: Event, context: ProcessFunction[Event, Event]#Context, metrics: Metrics): Unit = {

    println(s"***************** Start of Processing the Metabase Dashboard Event with Id = ${event._id}*****************")

    val admin = event.admin
    val targetedStateId = event.targetedState
    val targetedDistrictId = event.targetedDistrict
    val targetedProgramId = event.targetedProgram
    val targetedSolutionId = event.targetedSolution
    val solutions: String = config.solutions
    val projects: String = config.projects
    val tasks: String = config.tasks
    val metaDataTable = config.dashboard_metadata
    val report_config: String = config.report_config
    val metabaseDatabase: String = config.metabaseDatabase
    val domainName: String = config.metabaseDomainName

    // Printing the targetedState ID
    println(s"admin: $admin")
    println(s"Targeted State ID: $targetedStateId")
    println(s"Targeted District ID: $targetedDistrictId")
    println(s"Targeted Program ID: $targetedProgramId")
    println(s"Targeted Solution ID: $targetedSolutionId")

    event.reportType match {
      case "Project" =>
        println(s">>>>>>>>>>> Started Processing Metabase Project Dashboards >>>>>>>>>>>>")

        /**
         * Logic to process and create Admin Dashboard
         */
        if (admin.nonEmpty) {
          println(s"********** Started Processing Metabase Admin Dashboard ***********")
          val adminIdCheckQuery: String = s"""SELECT CASE WHEN main_metadata::jsonb @> '[{"collectionName":"Admin Collection"}]'::jsonb AND main_metadata::jsonb @> '[{"collectionName":"Project Collection"}]'::jsonb THEN 'Success' ELSE 'Failed' END AS result FROM $metaDataTable WHERE entity_id = '$admin';"""
          val adminIdStatus = postgresUtil.fetchData(adminIdCheckQuery) match {
            case List(map: Map[_, _]) => map.get("result").map(_.toString).getOrElse("")
            case _ => ""
          }
          if (adminIdStatus == "Failed") {
            try {
              val (adminCollectionName, adminCollectionDescription) = ("Admin Collection", "Admin Report")
              val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE id = '$admin';"
              val adminCollectionId: Int = CreateDashboard.checkAndCreateCollection(adminCollectionName, adminCollectionDescription, metabaseUtil, postgresUtil, createDashboardQuery)
              if (adminCollectionId != -1) {
                CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, "Report_Admin", adminCollectionId)
                val adminMetadataJson = new ObjectMapper().createArrayNode().add(new ObjectMapper().createObjectNode().put("collectionId", adminCollectionId).put("collectionName", adminCollectionName))
                postgresUtil.insertData(s"UPDATE $metaDataTable SET  main_metadata = COALESCE(main_metadata::jsonb, '[]'::jsonb) || '$adminMetadataJson' ::jsonb WHERE entity_id = '$admin';")
                val (projectCollectionName, projectCollectionDescription) = ("Project Collection", "This collection contains sub-collection, questions and dashboards required for Projects")
                val collectionId = Utils.createCollection(projectCollectionName, projectCollectionDescription, metabaseUtil, Some(adminCollectionId))
                val dashboardName: String = s"Project Admin Report"
                val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, dashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
                if (dashboardId != -1) {
                  val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
                  if (databaseId != -1) {
                    val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "state_name", postgresUtil, createDashboardQuery)
                    val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "district_name", postgresUtil, createDashboardQuery)
                    val programnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, solutions, "program_name", postgresUtil, createDashboardQuery)
                    val blocknameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "block_name", postgresUtil, createDashboardQuery)
                    val clusternameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "cluster_name", postgresUtil, createDashboardQuery)
                    val orgnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "org_name", postgresUtil, createDashboardQuery)
                    val reportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Admin';"
                    val mainQuestionCardIdList = UpdateAdminJsonFiles.ProcessAndUpdateJsonFiles(reportConfigQuery, collectionId, databaseId, dashboardId, statenameId, districtnameId, programnameId, blocknameId, clusternameId, orgnameId, projects, solutions, metabaseUtil, postgresUtil)
                    val mainQuestionIdsString = "[" + mainQuestionCardIdList.mkString(",") + "]"
                    val parametersQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Project-Parameter' AND question_type = 'admin-parameter'"
                    UpdateParameters.UpdateAdminParameterFunction(metabaseUtil, parametersQuery, dashboardId, postgresUtil)
                    val projectMetadataJson = new ObjectMapper().createArrayNode().add(new ObjectMapper().createObjectNode().put("collectionId", collectionId).put("collectionName", projectCollectionName).put("dashboardId", dashboardId).put("dashboardName", dashboardName).put("questionIds", mainQuestionIdsString))
                    postgresUtil.insertData(s"UPDATE $metaDataTable SET  main_metadata = COALESCE(main_metadata::jsonb, '[]'::jsonb) || '$projectMetadataJson' ::jsonb WHERE entity_id = '$admin';")

                    /**
                     * Mi dashboard Home page logic for Admin
                     */
                    val (mainCollectionName, mainCollectionDescription) = ("Mi Collection ", "This collection contains sub-collection, questions and dashboards required for MI DASHBOARD")
                    val mainCollectionId = Utils.checkAndCreateCollection(mainCollectionName, mainCollectionDescription, metabaseUtil, Some(collectionId))
                    if (mainCollectionId != -1) {
                      val homeDashboardName = "Mi Dashboard"
                      val homeDashboardId: Int = Utils.checkAndCreateDashboard(mainCollectionId, homeDashboardName, metabaseUtil, postgresUtil)
                      val homeReportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Mi-Dashboard' AND report_name = 'Home-Details-Report';"
                      val homeQuestionCardIdList = HomePage.ProcessAndUpdateJsonFiles(homeReportConfigQuery, mainCollectionId, databaseId, homeDashboardId, projects, solutions, report_config, metaDataTable, metabaseUtil, postgresUtil)
                      val homeQuestionIdsString = "[" + homeQuestionCardIdList.mkString(",") + "]"
                      val filterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Filters' AND question_type = 'home-dashboard-filter'"
                      val filterResults: List[Map[String, Any]] = postgresUtil.fetchData(filterQuery)
                      val objectMapper = new ObjectMapper()
                      val slugNameToStateIdFilterMap = mutable.Map[String, Int]()
                      for (result <- filterResults) {
                        val configString = result.get("config").map(_.toString).getOrElse("")
                        val configJson = objectMapper.readTree(configString)
                        val slugName = configJson.findValue("name").asText()
                        val stateIdFilter: Int = HomePage.updateAndAddFilter(metabaseUtil, configJson: JsonNode, mainCollectionId, databaseId, projects, solutions)
                        slugNameToStateIdFilterMap(slugName) = stateIdFilter
                      }
                      println(s"~~ Filter Question Id's $slugNameToStateIdFilterMap")
                      val parameterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Parameters' AND question_type = 'home-dashboard-parameter'"
                      val immutableSlugNameToStateIdFilterMap: Map[String, Int] = slugNameToStateIdFilterMap.toMap
                      println(immutableSlugNameToStateIdFilterMap)
                      HomePage.updateParameterFunction(metabaseUtil, postgresUtil, parameterQuery, immutableSlugNameToStateIdFilterMap, homeDashboardId)
                      val homeMetadataJson = new ObjectMapper().createArrayNode().add(new ObjectMapper().createObjectNode().put("collectionId", mainCollectionId).put("collectionName", mainCollectionName).put("dashboardId", homeDashboardId).put("dashboardName", homeDashboardName).put("questionIds", homeQuestionIdsString))
                      postgresUtil.insertData(s"UPDATE $metaDataTable SET  mi_metadata = '$homeMetadataJson' WHERE entity_id = '$admin';")

                      /**
                       * Mi dashboard Compare page logic for Admin
                       */
                      val (compareCollectionName, compareCollectionDescription) = (s"Comparison Collection", s"This collection contains questions and dashboard")
                      val compareCollectionId = Utils.checkAndCreateCollection(compareCollectionName, compareCollectionDescription, metabaseUtil, Some(mainCollectionId))
                      if (compareCollectionId != -1) {
                        val compareDashboardName = "Compare Dashboard"
                        val compareDashboardId: Int = Utils.checkAndCreateDashboard(compareCollectionId, compareDashboardName, metabaseUtil, postgresUtil)
                        val compareReportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Mi-Dashboard' AND report_name = 'Compare-Details-Report';"
                        val compareReportQuestionIdList = ComparePage.ProcessAndUpdateJsonFiles(compareReportConfigQuery, compareCollectionId, databaseId, compareDashboardId, statenameId, districtnameId, projects, solutions, metabaseUtil, postgresUtil)
                        val compareQuestionIdsString = "[" + compareReportQuestionIdList.mkString(",") + "]"
                        val compareParametersQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Parameters' AND question_type = 'compare-dashboard-parameter'"
                        ComparePage.UpdateAdminParameterFunction(metabaseUtil, compareParametersQuery, compareDashboardId, postgresUtil)
                        val compareMetadataJson = new ObjectMapper().createArrayNode().add(new ObjectMapper().createObjectNode().put("collectionId", compareCollectionId).put("collectionName", compareCollectionName).put("dashboardId", compareDashboardId).put("dashboardName", compareDashboardName).put("questionIds", compareQuestionIdsString))
                        postgresUtil.insertData(s"UPDATE $metaDataTable SET  comparison_metadata = '$compareMetadataJson' WHERE entity_id = '$admin';")
                        postgresUtil.insertData(s"UPDATE $metaDataTable SET status = 'Success', error_message = '' WHERE entity_id = '$admin';")
                      }
                    }
                  }
                }
              }
            } catch {
              case e: Exception =>
                postgresUtil.insertData(s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}' WHERE entity_id = '$admin';")
                println(s"An error occurred: ${e.getMessage}")
                e.printStackTrace()
            }
            println(s"********** Completed Processing Metabase Admin Dashboard ***********")
          } else {
            println(s"Project Dashboard has already created hence skipping the process !!!!!!!!!!!!")
          }
        } else {
          println(s"admin key is not present or is empty")
        }

        /**
         * Logic to process and create State Dashboard
         */
        if (targetedStateId.nonEmpty) {
          println(s"********** Started Processing Metabase State Dashboard ***********")
          val stateIdCheckQuery: String = s"SELECT CASE WHEN EXISTS (SELECT 1 FROM $metaDataTable WHERE entity_id = '$targetedStateId') THEN CASE WHEN COALESCE((SELECT status FROM $metaDataTable WHERE entity_id = '$targetedStateId'), '') = 'Success' THEN 'Success' ELSE 'Failed' END ELSE 'Failed' END AS result;"
          val stateIdStatus = postgresUtil.fetchData(stateIdCheckQuery) match {
            case List(map: Map[_, _]) => map.get("result").map(_.toString).getOrElse("")
            case _ => ""
          }
          if (stateIdStatus == "Failed") {
            try {
              val stateNameQuery = s"SELECT entity_name from $metaDataTable where entity_id = '$targetedStateId'"
              val stateName = postgresUtil.fetchData(stateNameQuery) match {
                case List(map: Map[_, _]) => map.get("entity_name").map(_.toString).getOrElse("")
                case _ => ""
              }
              println(s"stateName = $stateName")
              val collectionName = s"State Collection [$stateName]"
              val dashboardName = s"Project State Report [$stateName]"
              val groupName: String = s"${stateName}_State_Manager"
              val parametersQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Project-Parameter' AND question_type = 'state-parameter'"
              val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE entity_id = '$targetedStateId';"
              val collectionId: Int = CreateDashboard.checkAndCreateCollection(collectionName, s"State Report [$stateName]", metabaseUtil, postgresUtil, createDashboardQuery)
              if (collectionId != -1) {
                CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, groupName, collectionId)
                val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, dashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
                if (dashboardId != -1) {
                  val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
                  if (databaseId != -1) {
                    val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "state_name", postgresUtil, createDashboardQuery)
                    val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "district_name", postgresUtil, createDashboardQuery)
                    val programnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, solutions, "program_name", postgresUtil, createDashboardQuery)
                    val blocknameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "block_name", postgresUtil, createDashboardQuery)
                    val clusternameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "cluster_name", postgresUtil, createDashboardQuery)
                    val orgnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "org_name", postgresUtil, createDashboardQuery)
                    val reportConfigQuery: String = s"SELECT question_type , config FROM $report_config WHERE dashboard_name = 'State';"
                    val questionCardIdList = UpdateStateJsonFiles.ProcessAndUpdateJsonFiles(reportConfigQuery, collectionId, databaseId, dashboardId, statenameId, districtnameId, programnameId, blocknameId, clusternameId, orgnameId, projects, solutions, metabaseUtil, postgresUtil, targetedStateId)
                    val questionIdsString = "[" + questionCardIdList.mkString(",") + "]"
                    val filterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Project-Filters' AND question_type = 'state-filter'"
                    val filterResults: List[Map[String, Any]] = postgresUtil.fetchData(filterQuery)
                    val objectMapper = new ObjectMapper()
                    val slugNameToStateIdFilterMap = mutable.Map[String, Int]()
                    for (result <- filterResults) {
                      val configString = result.get("config").map(_.toString).getOrElse("")
                      val configJson = objectMapper.readTree(configString)
                      val slugName = configJson.findValue("name").asText()
                      val stateIdFilter: Int = UpdateAndAddStateFilter.updateAndAddFilter(metabaseUtil, configJson: JsonNode, s"$targetedStateId", collectionId, databaseId, projects, solutions)
                      slugNameToStateIdFilterMap(slugName) = stateIdFilter
                    }
                    val immutableSlugNameToStateIdFilterMap: Map[String, Int] = slugNameToStateIdFilterMap.toMap
                    UpdateParameters.updateParameterFunction(metabaseUtil, postgresUtil, parametersQuery, immutableSlugNameToStateIdFilterMap, dashboardId)
                    val adminMetadataJson = new ObjectMapper().createObjectNode().put("collectionId", collectionId).put("collectionName", collectionName).put("dashboardId", dashboardId).put("dashboardName", dashboardName).put("questionIds", questionIdsString)
                    postgresUtil.insertData(s"UPDATE $metaDataTable SET  main_metadata = '$adminMetadataJson' WHERE entity_id = '$targetedStateId';")

                    /**
                     * Logic to duplicate mi-state details page in Project collection
                     */
                    val getProjectCollectionIdQuery = s"SELECT jsonb_extract_path(element, 'collectionId') AS collection_id FROM $metaDataTable, LATERAL jsonb_array_elements(main_metadata::jsonb) AS element WHERE element ->> 'collectionName' = 'Project Collection' AND entity_name = 'Admin';"
                    val projectCollectionId = postgresUtil.executeQuery[Int](getProjectCollectionIdQuery)(resultSet => if (resultSet.next()) resultSet.getInt("collection_id") else 0)
                    val adminStateCollectionName = stateName
                    val adminStateDashboardName = s"$stateName - State Details"
                    val adminCompareDistrictCollectionName = s"Compare Districts of $stateName"
                    val adminCompareDistrictDashboardName = s"Compare $stateName Districts Dashboard"
                    val collectionListJson = mapper.readTree(metabaseUtil.listCollections())
                    val (miStateCollectionName, miStateCollectionDescription) = (s"State Collection", s"This collection contains questions and dashboards for all state")
                    val miStateCollectionId = collectionListJson.elements().asScala
                      .find(_.path("name").asText() == "State Collection")
                      .map(_.path("id").asInt())
                      .getOrElse(Utils.checkAndCreateCollection(miStateCollectionName, miStateCollectionDescription, metabaseUtil, Some(projectCollectionId)))
                    processMiStateDetailsPage(miStateCollectionId, adminStateCollectionName, adminStateDashboardName, adminCompareDistrictCollectionName, adminCompareDistrictDashboardName, stateName, databaseId, metabaseUtil, postgresUtil, "admin")

                    /**
                     * Logic to duplicate mi-state details page in State collection
                     */
                    val stateStateCollectionName = s"Mi Collection [$stateName]"
                    val stateStateDashboardName = s"Mi Dashboard [$stateName]"
                    val stateCompareDistrictCollectionName = s"Compare Districts [$stateName]"
                    val stateCompareDistrictDashboardName = s"Compare [$stateName] Districts Dashboard"
                    processMiStateDetailsPage(collectionId, stateStateCollectionName, stateStateDashboardName, stateCompareDistrictCollectionName, stateCompareDistrictDashboardName, stateName, databaseId, metabaseUtil, postgresUtil, "state")
                    postgresUtil.insertData(s"UPDATE $metaDataTable SET status = 'Success', error_message = '' WHERE entity_id = '$targetedStateId';")
                  }
                }
              }
            } catch {
              case e: Exception =>
                postgresUtil.insertData(s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}'  WHERE entity_id = '$targetedStateId';")
                println(s"An error occurred: ${e.getMessage}")
                e.printStackTrace()
            }
            println(s"********** Completed Processing Metabase State Dashboard ***********")
          } else {
            println(s"state report has already created hence skipping the process !!!!!!!!!!!!")
          }
        } else {
          println("targetedState is not present or is empty")
        }

        /**
         * Logic to process and create District Dashboard
         */
        if (targetedDistrictId.nonEmpty) {
          println(s"********** Started Processing Metabase District Dashboard ***********")
          val districtIdCheckQuery: String = s"SELECT CASE WHEN EXISTS (SELECT 1 FROM $metaDataTable WHERE entity_id = '$targetedDistrictId') THEN CASE WHEN COALESCE((SELECT status FROM $metaDataTable WHERE entity_id = '$targetedDistrictId'), '') = 'Success' THEN 'Success' ELSE 'Failed' END ELSE 'Failed' END AS result;"
          val districtIdStatus = postgresUtil.fetchData(districtIdCheckQuery) match {
            case List(map: Map[_, _]) => map.get("result").map(_.toString).getOrElse("")
            case _ => ""
          }
          if (districtIdStatus == "Failed") {
            try {
              val districtNameQuery = s"SELECT entity_name from $metaDataTable where entity_id = '$targetedDistrictId'"
              val districtName = postgresUtil.fetchData(districtNameQuery) match {
                case List(map: Map[_, _]) => map.get("entity_name").map(_.toString).getOrElse("")
                case _ => ""
              }
              val stateQuery = s"SELECT DISTINCT state_id AS id, state_name AS name FROM $projects WHERE district_id = '$targetedDistrictId'"
              val (stateId, stateName) = postgresUtil.fetchData(stateQuery) match {
                case List(map: Map[_, _]) =>
                  (map.get("id").map(_.toString).getOrElse(""), map.get("name").map(_.toString).getOrElse(""))
                case _ => ("", "")
              }
              println(s"stateId = $stateId")
              println(s"stateName = $stateName")
              println(s"districtName = $districtName")
              val collectionName = s"District collection [$districtName - $stateName]"
              val dashboardName = s"Project District Report [$districtName - $stateName]"
              val groupName: String = s"${districtName}_District_Manager[$stateName]"
              val parametersQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Project-Parameter' AND question_type = 'district-parameter'"
              val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE entity_id = '$targetedDistrictId';"
              val collectionId: Int = CreateDashboard.checkAndCreateCollection(collectionName, s"District Report [$districtName - $stateName]", metabaseUtil, postgresUtil, createDashboardQuery)
              if (collectionId != -1) {
                CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, groupName, collectionId)
                val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, dashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
                if (dashboardId != -1) {
                  val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
                  if (databaseId != -1) {
                    val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "state_name", postgresUtil, createDashboardQuery)
                    val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "district_name", postgresUtil, createDashboardQuery)
                    val programnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, solutions, "program_name", postgresUtil, createDashboardQuery)
                    val blocknameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "block_name", postgresUtil, createDashboardQuery)
                    val clusternameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "cluster_name", postgresUtil, createDashboardQuery)
                    val orgnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "org_name", postgresUtil, createDashboardQuery)
                    val reportConfigQuery: String = s"SELECT question_type , config FROM $report_config WHERE dashboard_name = 'District';"
                    val questionCardIdList = UpdateDistrictJsonFiles.ProcessAndUpdateJsonFiles(reportConfigQuery, collectionId, databaseId, dashboardId, statenameId, districtnameId, programnameId, blocknameId, clusternameId, orgnameId, metabaseUtil, postgresUtil, projects, solutions, targetedStateId, targetedDistrictId)
                    val questionIdsString = "[" + questionCardIdList.mkString(",") + "]"
                    val filterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Project-Filters' AND question_type = 'district-filter'"
                    val filterResults: List[Map[String, Any]] = postgresUtil.fetchData(filterQuery)
                    val objectMapper = new ObjectMapper()
                    val slugNameToDistrictIdFilterMap = mutable.Map[String, Int]()
                    for (result <- filterResults) {
                      val configString = result.get("config").map(_.toString).getOrElse("")
                      val configJson = objectMapper.readTree(configString)
                      val slugName = configJson.findValue("name").asText()
                      val districtIdFilter: Int = UpdateAndAddDistrictFilter.updateAndAddFilter(metabaseUtil, configJson, targetedStateId, targetedDistrictId, collectionId, databaseId, projects, solutions)
                      slugNameToDistrictIdFilterMap(slugName) = districtIdFilter
                    }
                    val immutableSlugNameToDistrictIdFilterMap: Map[String, Int] = slugNameToDistrictIdFilterMap.toMap
                    UpdateParameters.updateParameterFunction(metabaseUtil, postgresUtil, parametersQuery, immutableSlugNameToDistrictIdFilterMap, dashboardId)

                    val districtMetadataJson = new ObjectMapper().createObjectNode().put("collectionId", collectionId).put("collectionName", collectionName).put("dashboardId", dashboardId).put("dashboardName", dashboardName).put("questionIds", questionIdsString)
                    postgresUtil.insertData(s"UPDATE $metaDataTable SET main_metadata = '$districtMetadataJson' WHERE entity_id = '$targetedDistrictId';")

                    /**
                     * Logic to duplicate mi-district details page in Admin collection
                     */
                    val getProjectCollectionIdQuery = s"SELECT jsonb_extract_path(element, 'collectionId') AS collection_id FROM $metaDataTable, LATERAL jsonb_array_elements(main_metadata::jsonb) AS element WHERE element ->> 'collectionName' = 'Project Collection' AND entity_name = 'Admin';"
                    val projectCollectionId = postgresUtil.executeQuery[Int](getProjectCollectionIdQuery)(resultSet => if (resultSet.next()) resultSet.getInt("collection_id") else 0)
                    val adminDistrictCollectionName = s"[$districtName - $stateName]"
                    val adminDistrictDashboardName = s"[$districtName - $stateName] - District Details"
                    val adminCollectionListJson = mapper.readTree(metabaseUtil.listCollections())
                    //              val adminCollectionId = adminCollectionListJson.elements().asScala.find(_.path("name").asText() == "Admin Collection").map(_.path("id").asInt()).getOrElse(0)
                    val (miAdminDistrictCollectionName, miAdminDistrictCollectionDescription) = (s"District Collection", s"This collection contains questions and dashboards for all districts")
                    val miAdminDistrictCollectionId = adminCollectionListJson.elements().asScala
                      .find(_.path("name").asText() == "District Collection")
                      .map(_.path("id").asInt())
                      .getOrElse(Utils.checkAndCreateCollection(miAdminDistrictCollectionName, miAdminDistrictCollectionDescription, metabaseUtil, Some(projectCollectionId)))
                    processMiDistrictDetailsPage(miAdminDistrictCollectionId, adminDistrictCollectionName, adminDistrictDashboardName, databaseId, metabaseUtil, postgresUtil, "admin", stateName, districtName)

                    /**
                     * Logic to duplicate mi-district details page in Admin collection
                     */
                    val stateDistrictCollectionName = s"$stateName [$districtName]"
                    val stateDistrictDashboardName = s"$stateName [$districtName] - District Details"
                    val stateCollectionListJson = mapper.readTree(metabaseUtil.listCollections())
                    val stateCollectionId = stateCollectionListJson.elements().asScala.find(_.path("name").asText() == s"State Collection [$stateName]").map(_.path("id").asInt()).getOrElse(0)
                    val (miStateDistrictCollectionName, miStateDistrictCollectionDescription) = (s"District Collection [$stateName]", s"This collection contains questions and dashboards for all districts")
                    val miStateDistrictCollectionId = stateCollectionListJson.elements().asScala
                      .find(_.path("name").asText() == s"District Collection [$stateName]")
                      .map(_.path("id").asInt())
                      .getOrElse(Utils.checkAndCreateCollection(miStateDistrictCollectionName, miStateDistrictCollectionDescription, metabaseUtil, Some(stateCollectionId)))
                    processMiDistrictDetailsPage(miStateDistrictCollectionId, stateDistrictCollectionName, stateDistrictDashboardName, databaseId, metabaseUtil, postgresUtil, "state", stateName, districtName, Some(stateId))

                    /**
                     * Logic to duplicate district details page in District collection
                     */
                    val districtStateCollectionName = s"Mi Collection [$districtName - $stateName]"
                    val districtStateDashboardName = s"Mi Dashboard [$districtName - $stateName]"
                    processMiDistrictDetailsPage(collectionId, districtStateCollectionName, districtStateDashboardName, databaseId, metabaseUtil, postgresUtil, "district", stateName, districtName)
                    postgresUtil.insertData(s"UPDATE $metaDataTable SET status = 'Success', error_message = '' WHERE entity_id = '$targetedDistrictId';")
                  }
                }
              }
            } catch {
              case e: Exception =>
                val updateTableQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}'  WHERE entity_id = '$targetedDistrictId';"
                postgresUtil.insertData(updateTableQuery)
                println(s"An error occurred: ${e.getMessage}")
                e.printStackTrace()
            }
            println(s"********** Completed Processing Metabase District Dashboard ***********")
          } else {
            println("district report has already created hence skipping the process !!!!!!!!!!!!")
          }
        } else {
          println("targetedDistrict key is either not present or empty")
        }

        /**
         * Logic to process and create Program Dashboard
         */
        if (targetedProgramId.nonEmpty) {
          println(s"********** Started Processing Metabase Program Dashboard ***********")
          val programIdCheckQuery =
            s"""SELECT CASE WHEN
              EXISTS (SELECT 1 FROM $metaDataTable, LATERAL jsonb_array_elements(main_metadata::jsonb) AS e WHERE entity_id = '$targetedProgramId' AND e ->> 'collectionName' = ('Program Collection [' || entity_name || ']'))
              AND
              EXISTS (SELECT 1 FROM $metaDataTable, LATERAL jsonb_array_elements(main_metadata::jsonb) AS e WHERE entity_id = '$targetedProgramId' AND e ->> 'collectionName' = 'Project Collection')
              THEN 'Success' ELSE 'Failed' END AS result""".stripMargin.replaceAll("\n", " ")
          val programIdStatus = postgresUtil.fetchData(programIdCheckQuery) match {
            case List(map: Map[_, _]) => map.get("result").map(_.toString).getOrElse("")
            case _ => ""
          }
          if (programIdStatus == "Failed") {
            try {
              val programNameQuery = s"SELECT entity_name from $metaDataTable where entity_id = '$targetedProgramId'"
              val programName = postgresUtil.fetchData(programNameQuery) match {
                case List(map: Map[_, _]) => map.get("entity_name").map(_.toString).getOrElse("")
                case _ => ""
              }
              println(s"Targeted Program Name = $programName")
              val programCollectionName = s"Program Collection [$programName]"
              val groupName: String = s"Program_Manager[$programName]"
              val createDashboardQuery = s"UPDATE $metaDataTable SET status = 'Failed',error_message = 'errorMessage'  WHERE entity_id = '$targetedProgramId';"
              val programCollectionId: Int = CreateDashboard.checkAndCreateCollection(programCollectionName, s"Program Report [$programName]", metabaseUtil, postgresUtil, createDashboardQuery)
              if (programCollectionId != -1) {
                CreateAndAssignGroup.createGroupToDashboard(metabaseUtil, groupName, programCollectionId)
                val programMetadataJson = new ObjectMapper().createArrayNode().add(new ObjectMapper().createObjectNode().put("collectionId", programCollectionId).put("collectionName", programCollectionName))
                postgresUtil.insertData(s"UPDATE $metaDataTable SET  main_metadata = COALESCE(main_metadata::jsonb, '[]'::jsonb) || '$programMetadataJson' ::jsonb WHERE entity_id = '$targetedProgramId';")

                val (projectProgramCollectionName, projectCollectionDescription) = ("Project Collection", "This collection contains sub-collection, questions and dashboards required for Projects")
                val projectCollectionId = Utils.createCollection(projectProgramCollectionName, projectCollectionDescription, metabaseUtil, Some(programCollectionId))
                val projectProgramMetadataJson = new ObjectMapper().createArrayNode().add(new ObjectMapper().createObjectNode().put("collectionId", projectCollectionId).put("collectionName", projectProgramCollectionName))
                postgresUtil.insertData(s"UPDATE $metaDataTable SET  main_metadata = COALESCE(main_metadata::jsonb, '[]'::jsonb) || '$projectProgramMetadataJson' ::jsonb WHERE entity_id = '$targetedProgramId';")


                val solutionNameQuery = s"SELECT entity_name from $metaDataTable where entity_id = '$targetedSolutionId'"
                val solutionName = postgresUtil.fetchData(solutionNameQuery) match {
                  case List(map: Map[_, _]) => map.get("entity_name").map(_.toString).getOrElse("")
                  case _ => ""
                }
                println(s"Targeted Solution Name = $solutionName")

                val (solutionCollectionName, solutionCollectionDescription) = (s"Project Collection [$solutionName]", "This collection contains questions and dashboards required for Solutions")
                val collectionId = Utils.checkAndCreateCollection(solutionCollectionName, solutionCollectionDescription, metabaseUtil, Some(projectCollectionId))
                if (collectionId != -1) {
                  val solutionDashboardName = s"Project Report [$solutionName]"
                  val dashboardId: Int = CreateDashboard.checkAndCreateDashboard(collectionId, solutionDashboardName, metabaseUtil, postgresUtil, createDashboardQuery)
                  if (dashboardId != -1) {
                    val databaseId: Int = CreateDashboard.getDatabaseId(metabaseDatabase, metabaseUtil)
                    if (databaseId != -1) {
                      val statenameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "state_name", postgresUtil, createDashboardQuery)
                      val districtnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "district_name", postgresUtil, createDashboardQuery)
                      val programnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, solutions, "program_name", postgresUtil, createDashboardQuery)
                      val blocknameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "block_name", postgresUtil, createDashboardQuery)
                      val clusternameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "cluster_name", postgresUtil, createDashboardQuery)
                      val orgnameId: Int = GetTableData.getTableMetadataId(databaseId, metabaseUtil, projects, "org_name", postgresUtil, createDashboardQuery)
                      val reportConfigQuery: String = s"SELECT question_type , config FROM $report_config WHERE dashboard_name = 'Program';"
                      val questionCardIdList = UpdateProgramJsonFiles.ProcessAndUpdateJsonFiles(reportConfigQuery, collectionId, databaseId, dashboardId, statenameId, districtnameId, programnameId, blocknameId, clusternameId, orgnameId, projects, solutions, tasks, metabaseUtil, postgresUtil, targetedProgramId)
                      val questionIdsString = "[" + questionCardIdList.mkString(",") + "]"
                      val filterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Project-Filters' AND question_type = 'program-filter'"
                      val filterResults: List[Map[String, Any]] = postgresUtil.fetchData(filterQuery)
                      val objectMapper = new ObjectMapper()
                      val slugNameToProgramIdFilterMap = mutable.Map[String, Int]()
                      for (result <- filterResults) {
                        val configString = result.get("config").map(_.toString).getOrElse("")
                        val configJson = objectMapper.readTree(configString)
                        val slugName = configJson.findValue("name").asText()
                        val programIdFilter: Int = UpdateAndAddProgramFilter.updateAndAddFilter(metabaseUtil, configJson, targetedProgramId, collectionId, databaseId, projects, solutions)
                        slugNameToProgramIdFilterMap(slugName) = programIdFilter
                      }
                      val immutableSlugNameToProgramIdFilterMap: Map[String, Int] = slugNameToProgramIdFilterMap.toMap
                      val parametersQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Project-Parameter' AND question_type = 'program-parameter'"
                      UpdateParameters.updateParameterFunction(metabaseUtil, postgresUtil, parametersQuery, immutableSlugNameToProgramIdFilterMap, dashboardId)
                      val solutionMetadataJson = new ObjectMapper().createObjectNode().put("collectionId", collectionId).put("collectionName", solutionCollectionName).put("dashboardId", dashboardId).put("dashboardName", solutionDashboardName).put("questionIds", questionIdsString)
                      postgresUtil.insertData(s"UPDATE $metaDataTable SET  main_metadata = '$solutionMetadataJson', status = 'Success' WHERE entity_id = '$targetedSolutionId';")
                    }
                  }
                }
              }
            } catch {
              case e: Exception =>
                postgresUtil.insertData(s"UPDATE $metaDataTable SET status = 'Failed',error_message = '${e.getMessage}'  WHERE entity_id = '$targetedProgramId';")
                println(s"An error occurred: ${e.getMessage}")
                e.printStackTrace()
            }
            println(s"********** Completed Processing Metabase Program Dashboard ***********")
          } else {
            println("Solution report has already created hence skipping the process !!!!!!!!!!!!")
          }
        } else {
          println("targetedProgram key is either not present or empty")
        }

        println(s"***************** End of Processing the Metabase Project Dashboard *****************\n")
    }

    def processMiStateDetailsPage(parentCollectionId: Int, stateCollectionName: String, stateDashboardName: String, compareDistrictCollectionName: String, compareDistrictDashboardName: String, stateName: String, databaseId: Int, metabaseUtil: MetabaseUtil, postgresUtil: PostgresUtil, processType: String): Unit = {
      /**
       * Mi dashboard State Details page logic for State Manager
       */
      val stateCollectionDescription = s"This collection contains questions and dashboards for state $stateName"
      val stateCollectionId = Utils.checkAndCreateCollection(stateCollectionName, stateCollectionDescription, metabaseUtil, Some(parentCollectionId))
      if (stateCollectionId != -1) {
        val stateDashboardId: Int = Utils.checkAndCreateDashboard(stateCollectionId, stateDashboardName, metabaseUtil, postgresUtil)
        val stateReportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Mi-Dashboard' AND report_name = 'State-Details-Report';"
        val stateQuestionCardIdList = StatePage.ProcessAndUpdateJsonFiles(stateReportConfigQuery, stateCollectionId, databaseId, stateDashboardId, projects, solutions, metaDataTable, report_config, metabaseUtil, postgresUtil, targetedStateId, stateName)
        val stateQuestionIdsString = "[" + stateQuestionCardIdList.mkString(",") + "]"
        val stateFilterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Filters' AND question_type = 'state-dashboard-filter'"
        val stateFilterResults: List[Map[String, Any]] = postgresUtil.fetchData(stateFilterQuery)
        val stateObjectMapper = new ObjectMapper()
        val slugNameToStateIdFilterMapForState = mutable.Map[String, Int]()
        for (result <- stateFilterResults) {
          val configString = result.get("config").map(_.toString).getOrElse("")
          val configJson = stateObjectMapper.readTree(configString)
          val slugName = configJson.findValue("name").asText()
          val stateIdFilter: Int = StatePage.updateAndAddFilter(metabaseUtil, configJson: JsonNode, targetedStateId, stateCollectionId, databaseId, projects, solutions)
          slugNameToStateIdFilterMapForState(slugName) = stateIdFilter
        }
        val parameterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Parameters' AND question_type = 'state-dashboard-parameter'"
        val stateImmutableSlugNameToStateIdFilterMap: Map[String, Int] = slugNameToStateIdFilterMapForState.toMap
        StatePage.updateParameterFunction(metabaseUtil, postgresUtil, parameterQuery, stateImmutableSlugNameToStateIdFilterMap, stateDashboardId)
        val objectMapper = new ObjectMapper()
        if (processType == "admin") {
          val query = s"SELECT mi_metadata FROM $metaDataTable WHERE entity_type = '$processType'"
          val existingDataArray = postgresUtil.fetchData(query)
            .headOption.flatMap(_.get("mi_metadata"))
            .map(_.toString).map(objectMapper.readTree)
            .collect { case arr: ArrayNode => arr; case obj: ObjectNode => objectMapper.createArrayNode().add(obj) }
            .getOrElse(objectMapper.createArrayNode())
          val newMetadataJson = existingDataArray.add(objectMapper.createObjectNode().put("collectionId", stateCollectionId).put("collectionName", stateCollectionName).put("dashboardId", stateDashboardId).put("dashboardName", stateDashboardName).put("questionIds", stateQuestionIdsString))
          postgresUtil.insertData(s"UPDATE $metaDataTable SET mi_metadata = '$newMetadataJson' WHERE entity_id = '$admin';")
          postgresUtil.insertData(s"UPDATE $metaDataTable SET state_details_url_admin = '$domainName$stateDashboardId' WHERE entity_id = '$targetedStateId';")
        } else {
          val stateMetadataJson = new ObjectMapper().createArrayNode().add(new ObjectMapper().createObjectNode().put("collectionId", stateCollectionId).put("collectionName", stateCollectionName).put("dashboardId", stateDashboardId).put("dashboardName", stateDashboardName).put("questionIds", stateQuestionIdsString))
          postgresUtil.insertData(s"UPDATE $metaDataTable SET mi_metadata = '$stateMetadataJson', state_details_url_state = '$domainName$stateDashboardId' WHERE entity_id = '$targetedStateId';")
        }

        /**
         * Mi dashboard Compare District page logic for State Manager
         */
        val compareDistrictsDesc = s"This collection contains questions and dashboard"
        val compareDistrictCollectionId = Utils.checkAndCreateCollection(compareDistrictCollectionName, compareDistrictsDesc, metabaseUtil, Some(stateCollectionId))
        if (compareDistrictCollectionId != -1) {
          val compareDistrictDashboardId: Int = Utils.checkAndCreateDashboard(compareDistrictCollectionId, compareDistrictDashboardName, metabaseUtil, postgresUtil)
          val compareReportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Mi-Dashboard' AND report_name = 'Compare-District-Details-Report';"
          val compareDistrictReportQuestionCardIdList = StatePage.ProcessAndUpdateJsonFiles(compareReportConfigQuery, compareDistrictCollectionId, databaseId, compareDistrictDashboardId, projects, solutions, metaDataTable, report_config, metabaseUtil, postgresUtil, targetedStateId, stateName)
          val compareDistrictReportQuestionIdsString = "[" + compareDistrictReportQuestionCardIdList.mkString(",") + "]"
          val compareReportFilterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Filters' AND question_type = 'compare-district-dashboard-filter'"
          val compareReportFilterResults: List[Map[String, Any]] = postgresUtil.fetchData(compareReportFilterQuery)
          val compareDistrictObjectMapper = new ObjectMapper()
          val slugNameToDistrictIdFilterMap = mutable.Map[String, Int]()
          for (result <- compareReportFilterResults) {
            val configString = result.get("config").map(_.toString).getOrElse("")
            val configJson = compareDistrictObjectMapper.readTree(configString)
            val slugName = configJson.findValue("name").asText()
            val districtIdFilter: Int = StatePage.updateAndAddFilter(metabaseUtil, configJson: JsonNode, targetedStateId, compareDistrictCollectionId, databaseId, projects, solutions)
            slugNameToDistrictIdFilterMap(slugName) = districtIdFilter
          }
          val compareReportParameterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Parameters' AND question_type = 'compare-district-dashboard-parameter'"
          val immutableSlugNameToDistrictIdFilterMap: Map[String, Int] = slugNameToDistrictIdFilterMap.toMap
          StatePage.updateParameterFunction(metabaseUtil, postgresUtil, compareReportParameterQuery, immutableSlugNameToDistrictIdFilterMap, compareDistrictDashboardId)

          val compareReportObjectMapper = new ObjectMapper()
          if (processType == "admin") {
            val query = s"SELECT comparison_metadata FROM $metaDataTable WHERE entity_type = '$processType'"
            val existingDataArray = postgresUtil.fetchData(query)
              .headOption.flatMap(_.get("comparison_metadata"))
              .map(_.toString).map(compareReportObjectMapper.readTree)
              .collect { case arr: ArrayNode => arr; case obj: ObjectNode => compareReportObjectMapper.createArrayNode().add(obj) }
              .getOrElse(compareReportObjectMapper.createArrayNode())
            val newMetadataJson = existingDataArray.add(compareReportObjectMapper.createObjectNode().put("collectionId", compareDistrictCollectionId).put("collectionName", compareDistrictCollectionName).put("dashboardId", compareDistrictDashboardId).put("dashboardName", compareDistrictDashboardName).put("questionIds", compareDistrictReportQuestionIdsString))
            postgresUtil.insertData(s"UPDATE $metaDataTable SET comparison_metadata = '$newMetadataJson' WHERE entity_id = '$admin';")
          } else {
            val stateMetadataJson = compareReportObjectMapper.createObjectNode().put("collectionId", compareDistrictCollectionId).put("collectionName", compareDistrictCollectionName).put("dashboardId", compareDistrictDashboardId).put("dashboardName", compareDistrictDashboardName).put("questionIds", compareDistrictReportQuestionIdsString)
            postgresUtil.insertData(s"UPDATE $metaDataTable SET comparison_metadata = '$stateMetadataJson' WHERE entity_id = '$targetedStateId';")
          }
        }
      }
    }

    def processMiDistrictDetailsPage(parentCollectionId: Int, districtCollectionName: String, districtDashboardName: String, databaseId: Int, metabaseUtil: MetabaseUtil, postgresUtil: PostgresUtil, processType: String, stateName: String, districtName: String, stateId: Option[String] = None): Unit = {
      /**
       * Mi dashboard District Details page logic for State Manager
       */
      val districtCollectionDescription = s"This collection contains questions and dashboards for state $stateName"
      val districtCollectionId = Utils.checkAndCreateCollection(districtCollectionName, districtCollectionDescription, metabaseUtil, Some(parentCollectionId))
      if (districtCollectionId != -1) {
        val districtDashboardId: Int = Utils.checkAndCreateDashboard(districtCollectionId, districtDashboardName, metabaseUtil, postgresUtil)
        val districtReportConfigQuery: String = s"SELECT question_type, config FROM $report_config WHERE dashboard_name = 'Mi-Dashboard' AND report_name = 'District-Details-Report';"
        val districtQuestionCardIdList = DistrictPage.ProcessAndUpdateJsonFiles(districtReportConfigQuery, districtCollectionId, databaseId, districtDashboardId, projects, solutions, report_config, metabaseUtil, postgresUtil, targetedDistrictId, districtName)
        val districtQuestionIdsString = "[" + districtQuestionCardIdList.mkString(",") + "]"
        val districtFilterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Filters' AND question_type = 'district-dashboard-filter'"
        val districtFilterResults: List[Map[String, Any]] = postgresUtil.fetchData(districtFilterQuery)
        val districtObjectMapper = new ObjectMapper()
        val slugNameToStateIdFilterMapForState = mutable.Map[String, Int]()
        for (result <- districtFilterResults) {
          val configString = result.get("config").map(_.toString).getOrElse("")
          val configJson = districtObjectMapper.readTree(configString)
          val slugName = configJson.findValue("name").asText()
          val stateIdFilter: Int = DistrictPage.updateAndAddFilter(metabaseUtil, configJson: JsonNode, targetedDistrictId, districtCollectionId, databaseId, projects, solutions)
          slugNameToStateIdFilterMapForState(slugName) = stateIdFilter
        }
        val parameterQuery: String = s"SELECT config FROM $report_config WHERE report_name = 'Mi-Dashboard-Parameters' AND question_type = 'district-dashboard-parameter'"
        val stateImmutableSlugNameToStateIdFilterMap: Map[String, Int] = slugNameToStateIdFilterMapForState.toMap
        DistrictPage.updateParameterFunction(metabaseUtil, postgresUtil, parameterQuery, stateImmutableSlugNameToStateIdFilterMap, districtDashboardId)
        val objectMapper = new ObjectMapper()
        if (processType == "admin") {
          val query = s"SELECT mi_metadata FROM $metaDataTable WHERE entity_type = '$processType'"
          val existingDataArray = postgresUtil.fetchData(query)
            .headOption.flatMap(_.get("mi_metadata"))
            .map(_.toString).map(objectMapper.readTree)
            .collect { case arr: ArrayNode => arr; case obj: ObjectNode => objectMapper.createArrayNode().add(obj) }
            .getOrElse(objectMapper.createArrayNode())
          val newMetadataJson = existingDataArray.add(objectMapper.createObjectNode().put("collectionId", districtCollectionId).put("collectionName", districtCollectionName).put("dashboardId", districtDashboardId).put("dashboardName", districtDashboardName).put("questionIds", districtQuestionIdsString))
          postgresUtil.insertData(s"UPDATE $metaDataTable SET mi_metadata = '$newMetadataJson' WHERE entity_id = '$admin';")
          postgresUtil.insertData(s"UPDATE $metaDataTable SET district_details_url_admin = '$domainName$districtDashboardId' WHERE entity_id = '$targetedDistrictId';")
        } else if (processType == "state") {
          val query = s"SELECT mi_metadata FROM $metaDataTable WHERE entity_id = '${stateId.getOrElse("")}' "
          val existingDataArray = postgresUtil.fetchData(query)
            .headOption.flatMap(_.get("mi_metadata"))
            .map(_.toString).map(objectMapper.readTree)
            .collect { case arr: ArrayNode => arr; case obj: ObjectNode => objectMapper.createArrayNode().add(obj) }
            .getOrElse(objectMapper.createArrayNode())
          val newMetadataJson = existingDataArray.add(objectMapper.createObjectNode().put("collectionId", districtCollectionId).put("collectionName", districtCollectionName).put("dashboardId", districtDashboardId).put("dashboardName", districtDashboardName).put("questionIds", districtQuestionIdsString))
          postgresUtil.insertData(s"UPDATE $metaDataTable SET mi_metadata = '$newMetadataJson' WHERE entity_id = '${stateId.getOrElse("")}';")
          postgresUtil.insertData(s"UPDATE $metaDataTable SET district_details_url_state = '$domainName$districtDashboardId' WHERE entity_id = '$targetedDistrictId';")
        }
        else {
          val districtMetadataJson = objectMapper.createObjectNode().put("collectionId", districtCollectionId).put("collectionName", districtCollectionName).put("dashboardId", districtDashboardId).put("dashboardName", districtDashboardName).put("questionIds", districtQuestionIdsString)
          postgresUtil.insertData(s"UPDATE $metaDataTable SET mi_metadata = '$districtMetadataJson', district_details_url_district = '$domainName$districtDashboardId' WHERE entity_id = '$targetedDistrictId';")
        }
      }
    }

  }
}
