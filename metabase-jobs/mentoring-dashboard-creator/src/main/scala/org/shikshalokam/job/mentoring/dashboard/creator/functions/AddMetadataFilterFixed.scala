package org.shikshalokam.job.mentoring.dashboard.creator.functions

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.shikshalokam.job.util.MetabaseUtil

object AddMetadataFilterFixed {
  val objectMapper = new ObjectMapper()

  def createFilterQuestion(
                            metabaseUtil: MetabaseUtil,
                            filterJson: JsonNode,
                            collectionId: Int,
                            databaseId: Int,
                            tenantUserTable: String,
                            tenantOrgRolesTable: String,
                            orgId: Int
                          ): Int = {

    try {
      // Deep copy to avoid mutating original
      val filterCard = filterJson.deepCopy().asInstanceOf[ObjectNode]

      // Set collection ID
      filterCard.put("collection_id", collectionId)

      // Ensure dataset_query exists
      if (!filterCard.has("dataset_query")) {
        filterCard.set("dataset_query", objectMapper.createObjectNode())
      }

      val dqNode = filterCard.get("dataset_query").asInstanceOf[ObjectNode]

      // Ensure database ID
      dqNode.put("database", databaseId)

      // Ensure native -> template-tags exist (optional)
      if (!dqNode.has("native")) dqNode.set("native", objectMapper.createObjectNode())
      val nativeNode = dqNode.get("native").asInstanceOf[ObjectNode]
      if (!nativeNode.has("template-tags")) nativeNode.set("template-tags", objectMapper.createObjectNode())

      // FIX: Make sure visualization_settings is always an object
      if (!filterCard.has("visualization_settings") || filterCard.get("visualization_settings").isNull) {
        filterCard.set("visualization_settings", objectMapper.createObjectNode())
      }

      // Set default display for filter (optional, depending on type)
      if (!filterCard.has("display") || filterCard.get("display").isNull) {
        filterCard.put("display", "table")
      }

      // Replace table placeholders in query if needed
      val updatedFilterCard = updateQueryPlaceholders(filterCard, tenantUserTable, tenantOrgRolesTable, orgId)

      // Create the card via Metabase API
      val responseStr = metabaseUtil.createQuestionCard(updatedFilterCard.toString)
      val responseJson = objectMapper.readTree(responseStr)
      val cardId = Option(responseJson.get("id")).map(_.asInt()).getOrElse(-1)

      println(s"✔ Filter '${filterCard.path("name").asText("")}' created with card_id=$cardId")
      cardId

    } catch {
      case ex: Exception =>
        println(s"[ERROR] Failed to create filter '${filterJson.path("name").asText("")}': ${ex.getMessage}")
        -1
    }
  }

  private def updateQueryPlaceholders(filterCard: ObjectNode,
                                      tenantUserTable: String,
                                      tenantOrgRolesTable: String,
                                      orgId: Int): ObjectNode = {
    val queryPath = filterCard.at("/dataset_query/native/query")
    if (!queryPath.isMissingNode && queryPath.isTextual) {
      val query = queryPath.asText()
        .replace("${tenantUserTable}", tenantUserTable)
        .replace("${tenantOrgRolesTable}", tenantOrgRolesTable)
        .replace("${orgId}", orgId.toString)
      filterCard.at("/dataset_query/native").asInstanceOf[ObjectNode].put("query", query)
    }
    filterCard
  }
}
