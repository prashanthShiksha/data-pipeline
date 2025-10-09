package org.shikshalokam.job.mentoring.dashboard.creator.functions

import com.fasterxml.jackson.databind.node.{ArrayNode, JsonNodeFactory, ObjectNode, TextNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.postgresql.util.PGobject
import org.shikshalokam.job.util.{MetabaseUtil, PostgresUtil}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object ProcessTenantConstructor {

  /**
   * Processes report config JSONs, creates question cards in Metabase,
   * and returns the full JSON of created cards for dashboard addition.
   */
  def ProcessAndUpdateJsonFiles(
                                 reportConfigQuery: String,
                                 collectionId: Int,
                                 databaseId: Int,
                                 dashboardId: Int,
                                 statenameId: Int,
                                 districtnameId: Int,
                                 blocknameId: Int,
                                 clusternameId: Int,
                                 schoolnameId: Int,
                                 tenantUserTable: String,
                                 tenantSessionTable: String,
                                 tenantSessionAttendanceTable: String,
                                 tenantConnectionsTable: String,
                                 tenantOrgMentorRatingTable: String,
                                 tenantOrgRolesTable: String,
                                 orgId: Int,
                                 immutableSlugNameToFilterMap: Map[String, Int] = Map.empty,
                                 metabaseUtil: MetabaseUtil,
                                 postgresUtil: PostgresUtil
                               ): ListBuffer[ObjectNode] = {

    println(s"---------------Started ProcessAndUpdateJsonFiles----------------")
    val createdCards = ListBuffer[ObjectNode]()
    val objectMapper = new ObjectMapper()
    val queryResult = postgresUtil.fetchData(reportConfigQuery)

    queryResult.foreach { row =>
      row.get("config") match {
        case Some(queryValue: PGobject) =>
          val configJson = objectMapper.readTree(queryValue.getValue)
          if (configJson != null) {

            val originalQuestionCard = configJson.path("questionCard").deepCopy().asInstanceOf[ObjectNode]
            val chartName = Option(originalQuestionCard.path("name").asText()).getOrElse("Unknown Chart")

            // Replace filter card_ids from slug map
            val parametersNode = originalQuestionCard.path("parameters")
            if (parametersNode.isArray) {
              parametersNode.elements().asScala.foreach { param =>
                val slugNode = param.get("slug")
                val valuesSourceConfig = param.get("values_source_config")
                if (slugNode != null && slugNode.isTextual && valuesSourceConfig != null && valuesSourceConfig.isObject && valuesSourceConfig.has("card_id")) {
                  val slugValue = slugNode.asText()
                  immutableSlugNameToFilterMap.get(slugValue).foreach { replacementCardId =>
                    valuesSourceConfig.asInstanceOf[ObjectNode].put("card_id", replacementCardId)
                  }
                }
              }
            }

            // Update collection and database IDs
            val updatedQuestionCard = updateQuestionCardJsonValues(originalQuestionCard, collectionId, statenameId, districtnameId, blocknameId, clusternameId, schoolnameId, databaseId)

            // Update the query placeholders
            val finalQuestionCard = updatePostgresDatabaseQuery(
              updatedQuestionCard,
              tenantUserTable,
              tenantSessionTable,
              tenantSessionAttendanceTable,
              tenantConnectionsTable,
              tenantOrgMentorRatingTable,
              tenantOrgRolesTable,
              orgId
            ).asInstanceOf[ObjectNode]

            // Create question card in Metabase
            val cardResponse = objectMapper.readTree(metabaseUtil.createQuestionCard(finalQuestionCard.toString))
            val cardId = cardResponse.path("id").asInt()
            finalQuestionCard.put("id", cardId) // attach the Metabase card ID
            println(s">>>>>>>>> Successfully created question card '$chartName' with card_id: $cardId")

            // Append to the list of created cards
            createdCards.append(finalQuestionCard)
          }

        case None =>
          println("Key 'config' not found in the result row.")
      }
    }

    println(s"---------------Processed ProcessAndUpdateJsonFiles----------------")
    createdCards
  }

  // ------------------- Helper functions ------------------- //

  private def updateQuestionCardJsonValues(questionCard: ObjectNode, collectionId: Int, statenameId: Int, districtnameId: Int, blocknameId: Int, clusternameId: Int, schoolnameId: Int, databaseId: Int): ObjectNode = {
    questionCard.put("collection_id", collectionId)
    Option(questionCard.get("dataset_query")).foreach { datasetQuery =>
      val dqObj = datasetQuery.asInstanceOf[ObjectNode]
      dqObj.put("database", databaseId)

      Option(dqObj.get("native")).foreach { nativeNode =>
        Option(nativeNode.get("template-tags")).foreach { templateTags =>
          val params = Map(
            "select_state" -> statenameId,
            "select_district" -> districtnameId,
            "select_block" -> blocknameId,
            "select_cluster" -> clusternameId,
            "select_school" -> schoolnameId
          )
          params.foreach { case (paramName, paramId) =>
            Option(templateTags.get(paramName)).foreach { paramNode =>
              if (paramNode.isObject) {
                val objNode = paramNode.asInstanceOf[ObjectNode]
                if (objNode.has("dimension") && objNode.get("dimension").isArray) {
                  val dimNode = objNode.get("dimension").asInstanceOf[ArrayNode]
                  if (dimNode.size() >= 2) dimNode.set(1, dimNode.numberNode(paramId))
                }
              }
            }
          }
        }
      }
    }
    questionCard
  }

  private def updatePostgresDatabaseQuery(
                                           questionCard: ObjectNode,
                                           tenantUserTable: String,
                                           tenantSessionTable: String,
                                           tenantSessionAttendanceTable: String,
                                           tenantConnectionsTable: String,
                                           tenantOrgMentorRatingTable: String,
                                           tenantOrgRolesTable: String,
                                           orgId: Int
                                         ): ObjectNode = {
    val queryNode = questionCard.at("/dataset_query/native/query")
    if (queryNode.isMissingNode || !queryNode.isTextual) return questionCard

    val updatedQuery = queryNode.asText()
      .replace("${tenantUserTable}", tenantUserTable)
      .replace("${tenantSessionTable}", tenantSessionTable)
      .replace("${tenantSessionAttendanceTable}", tenantSessionAttendanceTable)
      .replace("${tenantConnectionsTable}", tenantConnectionsTable)
      .replace("${tenantOrgMentorRatingTable}", tenantOrgMentorRatingTable)
      .replace("${tenantOrgRolesTable}", tenantOrgRolesTable)
      .replace("${orgId}", orgId.toString)

    questionCard.at("/dataset_query/native").asInstanceOf[ObjectNode].set("query", TextNode.valueOf(updatedQuery))
    questionCard
  }

}
