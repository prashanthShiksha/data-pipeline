package org.shikshalokam.job.observation.dashboard.creator.functions

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.{ArrayNode, JsonNodeFactory, ObjectNode, TextNode}
import org.postgresql.util.PGobject
import org.shikshalokam.job.util.JSONUtil.mapper
import org.shikshalokam.job.util.{MetabaseUtil, PostgresUtil}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object UpdateDomainJsonFiles {
  def ProcessAndUpdateJsonFiles(reportConfigQuery: String, collectionId: Int, databaseId: Int, dashboardId: Int, statenameId: Int, districtnameId: Int, schoolId: Int, domain: String, metabaseUtil: MetabaseUtil, postgresUtil: PostgresUtil, report_config: String): ListBuffer[Int] = {
    println(s"---------------started processing ProcessAndUpdateJsonFiles function----------------")
    val questionCardId = ListBuffer[Int]()
    val objectMapper = new ObjectMapper()

    def processJsonFiles(collectionId: Int, databaseId: Int, dashboardId: Int, statenameId: Int, districtnameId: Int, schoolId: Int, report_config: String): Unit = {
      val queries = Map(
        "domainQuery" -> s"""SELECT distinct(domain) FROM "$domain" WHERE domain is not null ORDER BY domain;""",
        "criteriaQuery" -> s"""SELECT distinct(criteria) FROM "$domain" WHERE criteria is not null;""",
        "headingQuery" -> s"SELECT * FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'heading';",
        "domainChartQuery" -> s"SELECT * FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'domain-chart';",
        "criteriaChartQuery" -> s"SELECT * FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'criteria-chart';",
        "bigNumberChartQuery" -> s"SELECT * FROM $report_config WHERE dashboard_name = 'Domain' AND question_type = 'big-number';"
      )

      val results = queries.map { case (key, query) => key -> postgresUtil.fetchData(query) }
      var newRow = 0
      var newCol = 0

      def processChartResult(chartResult: List[Map[String, Any]], domainOrCriteriaName: String, isDomain: Boolean): Unit = {
        chartResult.foreach { row =>
          row.get("config") match {
            case Some(queryValue: PGobject) =>
              val configJson = objectMapper.readTree(queryValue.getValue)
              if (configJson != null) {
                val chartName = Option(configJson.path("questionCard").path("name").asText()).getOrElse("Unknown Chart")
                val updatedQuestionCard = updateQuestionCardJsonValues(configJson, collectionId, statenameId, districtnameId, schoolId, databaseId)
                val finalQuestionCard = updatePostgresDatabaseQuery(updatedQuestionCard, domain, if (isDomain) domainOrCriteriaName else "", if (!isDomain) domainOrCriteriaName else "")
                val cardId = mapper.readTree(metabaseUtil.createQuestionCard(finalQuestionCard.toString)).path("id").asInt()
                println(s">>>>>>>>> Successfully created question card with card_id: $cardId for $chartName")
                questionCardId.append(cardId)
                val updatedDashCard = updateQuestionIdInDashCard(configJson, cardId, newRow, newCol)
                newCol += configJson.path("dashCards").path("size_y").asInt() + 1
                AddQuestionCards.appendDashCardToDashboard(metabaseUtil, updatedDashCard, dashboardId)
              }
            case None => println("Key 'config' not found in the result row.")
          }
        }
      }

      def processHeadingResult(headingResult: List[Map[String, Any]], name: String): Unit = {
        headingResult.foreach { row =>
          row.get("config") match {
            case Some(queryValue: PGobject) =>
              val jsonString = queryValue.getValue
              val rootNode = objectMapper.readTree(jsonString)
              if (rootNode != null) {
                val dashCardsNode = rootNode.path("dashCards")
                val visualizationSettingsNode = dashCardsNode.path("visualization_settings")
                if (visualizationSettingsNode.has("text")) {
                  visualizationSettingsNode.asInstanceOf[ObjectNode].put("text", visualizationSettingsNode.get("text").asText().replace("${text}", name))
                }
                newCol += 3
                dashCardsNode.asInstanceOf[ObjectNode].put("col", newCol)
                dashCardsNode.asInstanceOf[ObjectNode].put("row", newRow)
                val optJsonNode = toOption(rootNode)
                AddQuestionCards.appendDashCardToDashboard(metabaseUtil, optJsonNode, dashboardId)
              }
            case None => println("Key 'config' not found in the result row.")
          }
        }
      }

      // Process big number charts
      processChartResult(results("bigNumberChartQuery"), "", isDomain = false)

      // Process domain charts
      results("domainQuery").foreach { row =>
        val domainName = row.get("domain").map(_.toString).getOrElse("")
        processHeadingResult(results("headingQuery"), domainName)
        processChartResult(results("domainChartQuery"), domainName, isDomain = true)
      }

      // Process criteria charts
      results("criteriaQuery").foreach { row =>
        val criteriaName = row.get("criteria").map(_.toString).getOrElse("")
        processHeadingResult(results("headingQuery"), criteriaName)
        processChartResult(results("criteriaChartQuery"), criteriaName, isDomain = false)
      }
    }

    def toOption(jsonNode: JsonNode): Option[JsonNode] = {
      if (jsonNode == null || jsonNode.isMissingNode) None else Some(jsonNode)
    }

    def updateQuestionIdInDashCard(json: JsonNode, cardId: Int, newRow: Int, newCol: Int): Option[JsonNode] = {
      Try {
        val jsonObject = json.asInstanceOf[ObjectNode]

        val dashCardsNode = if (jsonObject.has("dashCards") && jsonObject.get("dashCards").isObject) {
          jsonObject.get("dashCards").asInstanceOf[ObjectNode]
        } else {
          val newDashCardsNode = JsonNodeFactory.instance.objectNode()
          jsonObject.set("dashCards", newDashCardsNode)
          newDashCardsNode
        }

        // Update card_id
        dashCardsNode.put("card_id", cardId)

        // Update row and col
        dashCardsNode.put("row", newRow)
        dashCardsNode.put("col", newCol)

        // Update parameter_mappings if present
        if (dashCardsNode.has("parameter_mappings") && dashCardsNode.get("parameter_mappings").isArray) {
          dashCardsNode.get("parameter_mappings").elements().forEachRemaining { paramMappingNode =>
            if (paramMappingNode.isObject) {
              paramMappingNode.asInstanceOf[ObjectNode].put("card_id", cardId)
            }
          }
        }
        jsonObject
      }.toOption
    }

    def updateQuestionCardJsonValues(configJson: JsonNode, collectionId: Int, statenameId: Int, districtnameId: Int, schoolId: Int, databaseId: Int): JsonNode = {
      try {
        val configObjectNode = configJson.deepCopy().asInstanceOf[ObjectNode]
        Option(configObjectNode.get("questionCard")).foreach { questionCard =>
          questionCard.asInstanceOf[ObjectNode].put("collection_id", collectionId)

          Option(questionCard.get("dataset_query")).foreach { datasetQuery =>
            datasetQuery.asInstanceOf[ObjectNode].put("database", databaseId)

            Option(datasetQuery.get("native")).foreach { nativeNode =>
              Option(nativeNode.get("template-tags")).foreach { templateTags =>
                val params = Map(
                  "state_param" -> statenameId,
                  "district_param" -> districtnameId,
                  "school_param" -> schoolId
                )
                params.foreach { case (paramName, paramId) =>
                  Option(templateTags.get(paramName)).foreach { paramNode =>
                    updateDimension(paramNode.asInstanceOf[ObjectNode], paramId)
                  }
                }
              }
            }
          }
        }
        configObjectNode.get("questionCard")
      } catch {
        case e: Exception =>
          println(s"Warning: JSON node could not be updated. Error: ${e.getMessage}")
          configJson
      }
    }

    def updateDimension(node: JsonNode, newId: Int): Unit = {
      if (node.has("dimension") && node.get("dimension").isArray) {
        val dimensionNode = node.get("dimension").asInstanceOf[ArrayNode]
        if (dimensionNode.size() >= 2) {
          dimensionNode.set(1, JsonNodeFactory.instance.numberNode(newId))
        } else {
          println(s"Warning: 'dimension' array does not have enough elements to update.")
        }
      } else {
        println(s"Warning: 'dimension' node is missing or not an array.")
      }
    }

    def updatePostgresDatabaseQuery(json: JsonNode, domainTable: String, domainVal: String, criteriaVal: String): JsonNode = {
      Try {
        val queryNode = json.at("/dataset_query/native/query")
        if (queryNode.isMissingNode || !queryNode.isTextual) {
          throw new IllegalArgumentException("Query node is missing or not a valid string.")
        }

        val updatedQuery = queryNode.asText()
          .replace("${domainTable}", s""""$domainTable"""")
          .replace("${domain}", s"""'$domainVal'""")
          .replace("${criteria}", s"""'$criteriaVal'""")
        val updatedJson = json.deepCopy().asInstanceOf[ObjectNode]
        updatedJson.at("/dataset_query/native")
          .asInstanceOf[ObjectNode]
          .set("query", TextNode.valueOf(updatedQuery))
        updatedJson
      } match {
        case Success(updatedQueryJson) => updatedQueryJson
        case Failure(exception) =>
          throw new IllegalArgumentException("Failed to update query in JSON", exception)
      }
    }

    processJsonFiles(collectionId, databaseId, dashboardId, statenameId, districtnameId, schoolId, report_config)
    println(s"---------------processed ProcessAndUpdateJsonFiles function----------------")
    questionCardId
  }
}
