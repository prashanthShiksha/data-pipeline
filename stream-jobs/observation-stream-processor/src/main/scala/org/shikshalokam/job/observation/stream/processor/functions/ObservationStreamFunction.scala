package org.shikshalokam.job.observation.stream.processor.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.shikshalokam.job.observation.stream.processor.domain.Event
import org.shikshalokam.job.observation.stream.processor.task.ObservationStreamConfig
import org.shikshalokam.job.util.{PostgresUtil, ScalaJsonUtil}
import org.shikshalokam.job.{BaseProcessFunction, Metrics}
import org.slf4j.LoggerFactory

import java.util
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import scala.collection.immutable._

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
    if (event.status == "started" || event.status == "inprogress" || event.status == "submitted" || event.status == "completed") {
      println(s"***************** Start of Processing the Observation Event with Id = ${event._id} *****************")
      var userRoleIds: String = ""
      var userRoles: String = ""
      var orgId: String = ""
      var orgName: String = ""
      var orgCode: String = ""

      event.organisation.foreach { org =>
        if (org.get("code").contains(event.organisationId)) {
          orgName = org.get("name").map(_.toString).getOrElse("")
          orgId = org.get("id").map(_.toString).getOrElse("")
          orgCode = org.get("code").map(_.toString).getOrElse("")
          val roles = org.get("roles").map(_.asInstanceOf[List[Map[String, Any]]]).getOrElse(List.empty)
          val (userRoleIdsExtracted, userRolesExtracted) = extractUserRolesData(roles)
          userRoleIds = userRoleIdsExtracted
          userRoles = userRolesExtracted
        } else {
          println(s"Organisation with ID ${event.organisationId} not found in the event data.")
        }
      }

      def extractUserRolesData(roles: List[Map[String, Any]]): (String, String) = {
        if (roles == null || roles.isEmpty) {
          ("", "")
        } else {
          val roleId = roles.map { role => role.get("id").map(_.toString).getOrElse("") }
          val roleName = roles.map { role => role.get("title").map(_.toString).getOrElse("") }
          (roleId.mkString(", "), roleName.mkString(", "))
        }
      }

      println("\n==> domain data ")
      println("solutionId = " + event.solutionId)
      println("submissionId = " + event._id)
      println("userId = " + event.createdBy)
      println("submissionNumber = " + event.submissionNumber)
      println("stateName = " + event.stateName)
      println("districtName = " + event.districtName)
      println("blockName = " + event.blockName)
      println("clusterName = " + event.clusterName)
      println("schoolName = " + event.schoolName)
      println("status = " + event.status)
      println("tenantId = " + event.tenantId)
      println("solutionName = " + event.solutionName)
      println("userRoleIds = " + userRoleIds)
      println("userRoles = " + userRoles)
      println("organisationId = " + event.organisationId)
      println("organisationName = " + orgName)
      println("organisationCode = " + orgCode)

      val status_of_submission = event.status
      val submission_id = event._id
      val user_id = event.createdBy
      val submission_number = event.submissionNumber
      val state_name = event.stateName
      val state_id = event.stateId
      val district_name = event.districtName
      val district_id = event.districtId
      val block_name = event.blockName
      val block_id = event.blockId
      val cluster_name = event.clusterName
      val cluster_id = event.clusterId
      val school_name = event.schoolName
      val themes = event.themes
      val criteria_event = event.criteria
      val solution_id = event.solutionId
      val solution_name = event.solutionName
      val program_name = event.programName
      val program_id = event.programId
      val observation_name = event.observationName
      val observation_id = event.observationId
      val school_id = event.schoolId
      val tenant_id = event.tenantId
      val solution_external_id = event.solutionExternalId
      val solution_description = event.solutionDescription
      val program_external_id = event.programExternalId
      val program_description = event.programDescription
      val private_program = null
      val project_categories = null
      val project_duration = null
      val completed_date = event.completedDate
      val domainTable = s""""${solution_id}_domain""""
      val questionTable = s""""${solution_id}_questions""""
      val statusTable = s""""${solution_id}_status""""

      /**
       * Creating tables if not exists
       */

      def checkAndCreateTable(tableName: String, createTableQuery: String): Unit = {
        val checkTableExistsQuery =
          s"""SELECT EXISTS (
             |  SELECT FROM information_schema.tables
             |  WHERE table_name = '$tableName'
             |);
             |""".stripMargin

        val tableExists = postgresUtil.executeQuery(checkTableExistsQuery) { resultSet =>
          if (resultSet.next()) resultSet.getBoolean(1) else false
        }

        if (!tableExists) {
          postgresUtil.createTable(createTableQuery, tableName)
        }
      }

      if (status_of_submission == "completed") {
        postgresUtil.createTable(config.createSolutionsTable, config.solutions)
        postgresUtil.createTable(config.createDashboardMetadataTable, config.dashboard_metadata)


        val createDomainsTable =
          s"""CREATE TABLE IF NOT EXISTS $domainTable (
             |    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
             |    user_id TEXT ,
             |    user_role_ids TEXT,
             |    user_roles TEXT,
             |    solution_id TEXT ,
             |    solution_name TEXT,
             |    submission_id TEXT,
             |    submission_number INTEGER,
             |    program_name TEXT,
             |    program_id TEXT,
             |    observation_name TEXT,
             |    observation_id TEXT,
             |    tenant_id TEXT,
             |    org_name TEXT,
             |    org_id TEXT,
             |    org_code TEXT,
             |    state_name TEXT,
             |    state_id TEXT,
             |    district_name TEXT,
             |    district_id TEXT,
             |    block_name TEXT,
             |    block_id TEXT,
             |    cluster_name TEXT,
             |    cluster_id TEXT,
             |    school_name TEXT,
             |    school_id TEXT,
             |    domain TEXT,
             |    domain_level TEXT,
             |    criteria TEXT,
             |    criteria_level TEXT,
             |    completed_date TEXT
             |);""".stripMargin

        val AlterDomainTable =
          s"""ALTER TABLE IF EXISTS $domainTable
             |ADD COLUMN IF NOT EXISTS user_role_ids TEXT,
             |ADD COLUMN IF NOT EXISTS tenant_id TEXT,
             |ADD COLUMN IF NOT EXISTS org_code TEXT;
             |""".stripMargin

        val createQuestionsTable =
          s"""CREATE TABLE IF NOT EXISTS $questionTable (
             |    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
             |    user_id TEXT ,
             |    user_role_ids TEXT,
             |    user_roles TEXT,
             |    solution_id TEXT ,
             |    solution_name TEXT,
             |    submission_id TEXT,
             |    program_name TEXT,
             |    program_id TEXT,
             |    observation_name TEXT,
             |    observation_id TEXT,
             |    state_name TEXT,
             |    district_name TEXT,
             |    block_name TEXT,
             |    cluster_name TEXT,
             |    school_name TEXT,
             |    school_id TEXT,
             |    tenant_id TEXT,
             |    org_name TEXT,
             |    org_id TEXT,
             |    org_code TEXT,
             |    question_id TEXT,
             |    question_text TEXT,
             |    labels TEXT,
             |    value TEXT,
             |    score INTEGER,
             |    domain_name TEXT,
             |    criteria_name TEXT,
             |    has_parent_question BOOLEAN,
             |    parent_question_text TEXT,
             |    evidence TEXT,
             |    submitted_at TEXT,
             |    remarks TEXT,
             |    question_type TEXT
             |);""".stripMargin

        val AlterQuestionTable =
          s"""ALTER TABLE IF EXISTS $questionTable
             |ADD COLUMN IF NOT EXISTS user_role_ids TEXT,
             |ADD COLUMN IF NOT EXISTS tenant_id TEXT,
             |ADD COLUMN IF NOT EXISTS org_id TEXT,
             |ADD COLUMN IF NOT EXISTS org_code TEXT;
             |""".stripMargin

        checkAndCreateTable(domainTable, createDomainsTable)
        postgresUtil.executeUpdate(AlterDomainTable, domainTable, solution_id)
        checkAndCreateTable(questionTable, createQuestionsTable)
        postgresUtil.executeUpdate(AlterQuestionTable, questionTable, solution_id)

        val upsertSolutionQuery =
          s"""INSERT INTO ${config.solutions} (solution_id, external_id, name, description, duration, categories, program_id, program_name, program_external_id, program_description, private_program)
             |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
             |ON CONFLICT (solution_id) DO UPDATE SET
             |    external_id = ?,
             |    name = ?,
             |    description = ?,
             |    duration = ?,
             |    categories = ?,
             |    program_id = ?,
             |    program_name = ?,
             |    program_external_id = ?,
             |    program_description = ?,
             |    private_program = ?;
             |""".stripMargin

        val solutionParams = Seq(
          // Insert parameters
          solution_id, solution_external_id, solution_name, solution_description, project_duration, project_categories, program_id, program_name, program_external_id, program_description, private_program,

          // Update parameters (matching columns in the ON CONFLICT clause)
          solution_external_id, solution_name, solution_description, project_duration, project_categories, program_id, program_name, program_external_id, program_description, private_program
        )

        postgresUtil.executePreparedUpdate(upsertSolutionQuery, solutionParams, config.solutions, solution_id)


        /**
         * Extracting domain data
         */

        val deleteQuery =
          s"""DELETE FROM $domainTable
             |WHERE user_id = ?
             |  AND state_id = ?
             |  AND district_id = ?
             |  AND block_id = ?
             |  AND cluster_id = ?
             |  AND school_id = ?;
             |""".stripMargin

        val deleteParams = Seq(user_id, state_id, district_id, block_id, cluster_id, school_id)

        postgresUtil.executePreparedUpdate(deleteQuery, deleteParams, domainTable, user_id)

        themes.foreach { domain =>
          val domain_name = domain("name")
          val domain_level = domain.getOrElse("pointsBasedLevel", null)
          val maybeChildren = domain.get("children").map(_.asInstanceOf[List[Map[String, Any]]])
          val maybeCriteria = domain.get("criteria").map(_.asInstanceOf[List[Map[String, Any]]])

          maybeChildren match {
            case Some(subdomains) =>
              subdomains.foreach { subdomain =>
                val maybeSubCriteria = subdomain.get("criteria").map(_.asInstanceOf[List[Map[String, Any]]])
                maybeSubCriteria.foreach { criteriaList =>
                  criteriaList.foreach { criteria =>
                    val criteriaId = criteria("criteriaId")

                    criteria_event.find(_("_id") == criteriaId).foreach { crit =>
                    }
                  }
                }
              }

            case None =>
              maybeCriteria.foreach { criteriaList =>
                criteriaList.foreach { criteria =>
                  val criteriaId = criteria("criteriaId")
                  println(s"criteria_id: $criteriaId")

                  criteria_event.find(c => c("_id") == criteriaId).foreach { crit =>
                    val criteria_name = crit("name")
                    val criteria_level = crit("score")
                    val insertCriteriaQuery =
                      s"""INSERT INTO $domainTable (
                         |    user_id, user_role_ids, user_roles, solution_id, solution_name, submission_id, submission_number, program_name, program_id,
                         |    observation_name, observation_id, state_name, state_id, district_name, district_id, block_name, block_id, cluster_name,
                         |    cluster_id, tenant_id, org_name, org_id, org_code, school_name, school_id, domain, domain_level, criteria, criteria_level, completed_date
                         |) VALUES (
                         |        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                         |);
                         |""".stripMargin

                    val criteriaParams = Seq(
                      user_id, userRoleIds, userRoles, solution_id, solution_name, submission_id, submission_number, program_name,
                      program_id, observation_name, observation_id, state_name, state_id, district_name, district_id, block_name, block_id,
                      cluster_name, cluster_id, tenant_id, orgName, orgId, orgCode, school_name, school_id, domain_name, domain_level, criteria_name, criteria_level, completed_date
                    )

                    postgresUtil.executePreparedUpdate(insertCriteriaQuery, criteriaParams, domainTable, solution_id)
                  }
                }
              }
          }
        }

        /**
         * Extracting question data
         */

        val deleteQuestionQuery =
          s"""DELETE FROM $questionTable
             |WHERE user_id = ?
             |  AND state_name = ?
             |  AND district_name = ?
             |  AND block_name = ?
             |  AND cluster_name = ?
             |  AND school_name = ?;
             |""".stripMargin

        val deleteQuestionParams = Seq(user_id, state_name, district_name, block_name, cluster_name, school_name)

        postgresUtil.executePreparedUpdate(deleteQuestionQuery, deleteQuestionParams, questionTable, user_id)

        val questionsFunction = new ObservationQuestionFunction(postgresUtil, config, questionTable)
        val answersKey = event.answers

        def processQuestion(responseType: String, questionsMap: Map[String, Any], payload: Option[Map[String, Any]], question_id: String, domain_name: String,
                            criteria_name: String, has_parent_question: Boolean, parent_question_text: String, evidences: String, remarks: String): Unit = {
          val value: String = questionsMap.get("value") match {
            case Some(v: String) => v
            case Some(v: Int) => v.toString
            case _ => ""
          }
          val score: Integer = questionsMap.get("scoreAchieved") match {
            case Some(v: Integer) => v
            case _ => 0
          }

          responseType match {
            case "text" =>
              questionsFunction.textQuestionType(payload, question_id, solution_id, solution_name, submission_id, user_id, userRoleIds, userRoles, program_name, program_id,
                observation_name, observation_id, value, state_name, district_name, block_name, cluster_name, school_name, school_id, tenant_id, orgId, orgCode, orgName, domain_name, criteria_name, score,
                has_parent_question, parent_question_text, evidences, remarks, completed_date)
            case "radio" =>
              questionsFunction.radioQuestionType(payload, question_id, solution_id, solution_name, submission_id, user_id, userRoleIds, userRoles, program_name, program_id,
                observation_name, observation_id, value, state_name, district_name, block_name, cluster_name, school_name, school_id, tenant_id, orgId, orgCode, orgName, domain_name, criteria_name, score,
                has_parent_question, parent_question_text, evidences, remarks, completed_date)
            case "date" =>
              questionsFunction.dateQuestionType(payload, question_id, solution_id, solution_name, submission_id, user_id, userRoleIds, userRoles, program_name, program_id,
                observation_name, observation_id, value, state_name, district_name, block_name, cluster_name, school_name, school_id, tenant_id, orgId, orgCode, orgName, domain_name, criteria_name, score,
                has_parent_question, parent_question_text, evidences, remarks, completed_date)
            case "multiselect" =>
              questionsFunction.multiselectQuestionType(payload, question_id, solution_id, solution_name, submission_id, user_id, userRoleIds, userRoles, program_name, program_id,
                observation_name, observation_id, value, state_name, district_name, block_name, cluster_name, school_name, school_id, tenant_id, orgId, orgCode, orgName, domain_name, criteria_name, score,
                has_parent_question, parent_question_text, evidences, remarks, completed_date)
            case "number" =>
              questionsFunction.numberQuestionType(payload, question_id, solution_id, solution_name, submission_id, user_id, userRoleIds, userRoles, program_name, program_id,
                observation_name, observation_id, value, state_name, district_name, block_name, cluster_name, school_name, school_id, tenant_id, orgId, orgCode, orgName, domain_name, criteria_name, score,
                has_parent_question, parent_question_text, evidences, remarks, completed_date)
            case "slider" =>
              questionsFunction.sliderQuestionType(payload, question_id, solution_id, solution_name, submission_id, user_id, userRoleIds, userRoles, program_name, program_id,
                observation_name, observation_id, value, state_name, district_name, block_name, cluster_name, school_name, school_id, tenant_id, orgId, orgCode, orgName, domain_name, criteria_name, score,
                has_parent_question, parent_question_text, evidences, remarks, completed_date)
            case "matrix" =>
              questionsMap.get("value") match {
                case Some(valueList: List[Map[String, Any]]) =>
                  valueList.foreach { instance =>
                    instance.foreach { case (_, questionData) =>
                      val matrixQuestionMap = questionData.asInstanceOf[Map[String, Any]]
                      val matrixQuestionId: String = matrixQuestionMap.get("qid") match {
                        case Some(v: String) => v
                        case _ => ""
                      }
                      val matrixPayload = matrixQuestionMap.get("payload").map(_.asInstanceOf[Map[String, Any]])
                      val matrixResponseType = matrixQuestionMap.get("responseType").map(_.toString).getOrElse("")
                      processQuestion(matrixResponseType, matrixQuestionMap, matrixPayload, matrixQuestionId, domain_name, criteria_name, has_parent_question, parent_question_text, evidences, remarks)
                    }
                  }
                case _ => println("No matrix data found.")
              }
            case _ => println(s"Unsupported responseType: $responseType")
          }
        }

        answersKey match {
          case answersMap: Map[_, _] =>
            answersMap.foreach { case (_, value) =>
              val questionsMap = value.asInstanceOf[Map[String, Any]]
              val payloadOpt: Option[Map[String, Any]] = questionsMap.get("payload").collect { case m: Map[String @unchecked, Any @unchecked] => m }
              println(s"qid: ${questionsMap.get("qid")}")
              val question_id: String = questionsMap.get("qid") match {
                case Some(v: String) => v
                case _ => ""
              }
              val question_criteria_id: String = questionsMap.get("criteriaId") match {
                case Some(v: String) => v
                case _ => ""
              }
              var domain_name: String = ""
              var criteria_name: String = ""
              var remarks: String = questionsMap.get("remarks") match {
                case Some(v: String) => v
                case _ => ""
              }
              val evidences = questionsMap match {
                case map: Map[String, Any] if map.nonEmpty =>
                  val extractedEvidences = map.get("fileName") match {
                    case Some(fileList: List[Map[String, Any]]) =>
                      fileList.collect {
                        case file if file.contains("sourcePath") => file("sourcePath").toString
                      }
                    case _ => Seq.empty
                  }
                  if (extractedEvidences.isEmpty) null else extractedEvidences.mkString(",")
                case _ => null
              }

              val result = for {
                theme <- themes
                criteriaList = theme("criteria").asInstanceOf[List[Map[String, Any]]]
                matchingCriteria <- criteriaList.find(_("criteriaId") == question_criteria_id)
                themeName = theme("name").toString
                criteriaName <- criteria_event.find(_("_id") == question_criteria_id).map(_("name").toString)
              } yield (themeName, criteriaName)

              result.foreach { case (themeName, criteriaName) =>
                domain_name = themeName
                criteria_name = criteriaName
                println(s"Domain Name: $themeName, Criteria Name: $criteriaName")
              }

              val payload = questionsMap.get("payload") match {
                case Some(value: Map[String, Any]) => Some(value)
                case _ => None
              }
              val responseType = questionsMap.get("responseType").map(_.toString).getOrElse("")

              if (payloadOpt.isDefined) {
                if (responseType == "matrix") {
                  val parent_question_text: String = questionsMap.get("payload") match {
                    case Some(payloadMap: Map[_, _]) =>
                      payloadMap.asInstanceOf[Map[String, Any]].get("question") match {
                        case Some(qList: List[_]) =>
                          qList.collect { case q: String if q.nonEmpty => q }.headOption.getOrElse("")
                        case Some(q: String) => q
                        case _ => ""
                      }
                    case _ => ""
                  }
                  val has_parent_question: Boolean = parent_question_text.nonEmpty

                  processQuestion(responseType, questionsMap, payload, question_id, domain_name, criteria_name, has_parent_question, parent_question_text, evidences, remarks)
                } else {
                  processQuestion(responseType, questionsMap, payload, question_id, domain_name, criteria_name, false, null, evidences, remarks)
                }
              } else {
                println(s"Skipping question_id=$question_id as payload is missing.")
              }
            }
          case _ =>
            logger.error("Unexpected structure for answers field")
        }

        /**
         * Logic to populate kafka messages for creating metabase dashboard
         */
        val dashboardData = new java.util.HashMap[String, String]()
        val dashboardConfig = Seq(
          ("admin", "1", "admin"),
          ("program", program_id, "targetedProgram"),
          ("solution", solution_id, "targetedSolution")
        )

        dashboardConfig
          .filter { case (key, _, _) => config.reportsEnabled.contains(key) }
          .foreach { case (key, value, target) =>
            checkAndInsert(key, value, dashboardData, target)
          }
        dashboardData.put("isRubric", event.isRubric.toString)

        if (!dashboardData.isEmpty) {
          pushObservationDashboardEvents(dashboardData, context)
        }
      }

      def checkAndInsert(entityType: String, targetedId: String, dashboardData: java.util.HashMap[String, String], dashboardKey: String): Unit = {
        val query = s"SELECT EXISTS (SELECT 1 FROM ${config.dashboard_metadata} WHERE entity_id = '$targetedId') AS is_${entityType}_present"
        val result = postgresUtil.fetchData(query)

        result.foreach { row =>
          row.get(s"is_${entityType}_present") match {
            case Some(isPresent: Boolean) if isPresent =>
              println(s"$entityType details already exist.")
            case _ =>
              if (entityType == "admin") {
                val insertQuery = s"INSERT INTO ${config.dashboard_metadata} (entity_type, entity_name, entity_id) VALUES ('$entityType', 'Admin', '$targetedId')"
                val affectedRows = postgresUtil.insertData(insertQuery)
                println(s"Inserted Admin details. Affected rows: $affectedRows")
                dashboardData.put(dashboardKey, "1")
              } else {
                val getEntityNameQuery =
                  s"""
                     |SELECT DISTINCT ${
                    if (entityType == "solution") "name"
                    else s"${entityType}_name"
                  } AS ${entityType}_name
                     |FROM ${
                    entityType match {
                      case "program" => config.solutions
                      case "solution" => config.solutions
                    }
                  }
                     |WHERE ${entityType}_id = '$targetedId'
               """.stripMargin.replaceAll("\n", " ")
                val result = postgresUtil.fetchData(getEntityNameQuery)
                result.foreach { id =>
                  val entityName = id.get(s"${entityType}_name").map(_.toString).getOrElse("")
                  val upsertMetaDataQuery =
                    s"""INSERT INTO ${config.dashboard_metadata} (
                       |    entity_type, entity_name, entity_id
                       |) VALUES (
                       |    ?, ?, ?
                       |) ON CONFLICT (entity_id) DO UPDATE SET
                       |    entity_type = ?, entity_name = ?;
                       |""".stripMargin

                  val dashboardParams = Seq(
                    entityType, entityName, targetedId, // Insert parameters
                    entityType, entityName // Update parameters (matching columns in the ON CONFLICT clause)
                  )
                  postgresUtil.executePreparedUpdate(upsertMetaDataQuery, dashboardParams, config.dashboard_metadata, targetedId)
                  println(s"Inserted [$entityName : $targetedId] details.")
                  dashboardData.put(dashboardKey, targetedId)
                }
              }
          }
        }
      }

      if (status_of_submission != null) {
        val createStatusTable =
          s"""CREATE TABLE IF NOT EXISTS $statusTable (
             |    id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
             |    user_id TEXT ,
             |    user_role_ids TEXT,
             |    user_roles TEXT,
             |    solution_id TEXT ,
             |    solution_name TEXT,
             |    submission_id TEXT,
             |    program_name TEXT,
             |    program_id TEXT,
             |    observation_name TEXT,
             |    observation_id TEXT,
             |    state_name TEXT,
             |    district_name TEXT,
             |    block_name TEXT,
             |    cluster_name TEXT,
             |    school_name TEXT,
             |    school_id TEXT,
             |    tenant_id TEXT,
             |    org_id TEXT,
             |    org_code TEXT,
             |    org_name TEXT,
             |    status_of_submission TEXT,
             |    submitted_at TEXT
             |);""".stripMargin

        val AlterStatusTable =
          s"""ALTER TABLE IF EXISTS $questionTable
             |ADD COLUMN IF NOT EXISTS user_role_ids TEXT,
             |ADD COLUMN IF NOT EXISTS tenant_id TEXT,
             |ADD COLUMN IF NOT EXISTS org_id TEXT,
             |ADD COLUMN IF NOT EXISTS org_code TEXT;
             |""".stripMargin

        checkAndCreateTable(statusTable, createStatusTable)
        postgresUtil.executeUpdate(AlterStatusTable, statusTable, solution_id)

        val deleteQuery =
          s"""DELETE FROM $statusTable
             |WHERE user_id = ?
             |  AND state_name = ?
             |  AND district_name = ?
             |  AND block_name = ?
             |  AND cluster_name = ?
             |  AND school_name = ?;
             |""".stripMargin

        val deleteParams = Seq(user_id, state_name, district_name, block_name, cluster_name, school_name)

        postgresUtil.executePreparedUpdate(deleteQuery, deleteParams, statusTable, user_id)

        val insertStatusQuery =
          s"""INSERT INTO $statusTable (
             |    user_id, user_role_ids, user_roles, solution_id, solution_name, submission_id, program_name, program_id,
             |    observation_name, observation_id, state_name, district_name, block_name, cluster_name, tenant_id, org_id, org_code, org_name,
             |    school_name, school_id, status_of_submission, submitted_at
             |) VALUES (
             |        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
             |);
             |""".stripMargin

        val statusParams = Seq(
          user_id, userRoleIds, userRoles, solution_id, solution_name, submission_id, program_name,
          program_id, observation_name, observation_id, state_name, district_name, block_name, cluster_name,
          tenant_id, orgId, orgCode, orgName, school_name, school_id, status_of_submission, completed_date
        )

        postgresUtil.executePreparedUpdate(insertStatusQuery, statusParams, statusTable, solution_id)
      }

      def pushObservationDashboardEvents(dashboardData: util.HashMap[String, String], context: ProcessFunction[Event, Event]#Context): util.HashMap[String, AnyRef] = {
        val objects = new util.HashMap[String, AnyRef]() {
          put("_id", java.util.UUID.randomUUID().toString)
          put("reportType", "Observation")
          put("publishedAt", DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(System.currentTimeMillis())).asInstanceOf[AnyRef])
          put("dashboardData", dashboardData)
        }
        val event = ScalaJsonUtil.serialize(objects)
        context.output(config.eventOutputTag, event)
        println(s"----> Pushed new Kafka message to ${config.outputTopic} topic")
        println(objects)
        objects
      }
    } else {
      println(s"Skipping the observation event with Id = ${event._id} and status = ${event.status} as it is not in a valid status.")
    }
  }
}