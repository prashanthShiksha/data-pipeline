package org.shikshalokam.job.mentoring.dashboard.creator.functions

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.shikshalokam.job.util.MetabaseUtil
import com.fasterxml.jackson.databind.node.JsonNodeFactory


import scala.collection.immutable._


object AddMetadataFilter {
  val objectMapper = new ObjectMapper()

  /**
   * Creates filter question cards for metadata (org, state, etc.)
   * Replaces placeholders and returns the newly created question card ID.
   */
  def updateAndAddFilter(
                          metabaseUtil: MetabaseUtil,
                          queryResult: JsonNode,
                          collectionId: Int,
                          databaseId: Int,
                          tenantUserTable: String,
                          tenantSessionTable: String,
                          tenantSessionAttendanceTable: String,
                          tenantConnectionsTable: String,
                          tenantOrgMentorRatingTable: String,
                          tenantOrgRolesTable: String,
                          orgId: Int
                        ): Int = {

    println(s"🚀 [INFO] Started creating metadata filter question for orgId=$orgId")

    // Define replacements
    val replacements: Map[String, String] = Map(
      "${tenantUserTable}" -> tenantUserTable,
      "${tenantSessionTable}" -> tenantSessionTable,
      "${tenantSessionAttendanceTable}" -> tenantSessionAttendanceTable,
      "${tenantConnectionsTable}" -> tenantConnectionsTable,
      "${tenantOrgMentorRatingTable}" -> tenantOrgMentorRatingTable,
      "${tenantOrgRolesTable}" -> tenantOrgRolesTable,
      "${orgId}" -> orgId.toString // ✅ integer replacement
    )

    /** Recursively replace placeholders */
    def replacePlaceholders(json: JsonNode): JsonNode = {
      def processNode(node: JsonNode): JsonNode = node match {
        case obj: ObjectNode =>
          val fields = obj.fieldNames()
          while (fields.hasNext) {
            val fieldName = fields.next()
            val childNode = obj.get(fieldName)
            if (childNode.isTextual) {
              var updatedText = childNode.asText()
              replacements.foreach { case (placeholder, actual) =>
                if (updatedText.contains(placeholder))
                  updatedText = updatedText.replace(placeholder, actual)
              }
              obj.put(fieldName, updatedText)
            } else {
              obj.set(fieldName, processNode(childNode))
            }
          }
          obj

        case array: ArrayNode =>
          val newArray = objectMapper.createArrayNode()
          array.elements().forEachRemaining { child =>
            newArray.add(processNode(child))
          }
          newArray

        case _ => node
      }

      processNode(json.deepCopy())
    }

    /** Update collectionId and databaseId */
    def updateIds(jsonFile: JsonNode): JsonNode = {
      val questionCardNode = jsonFile.get("questionCard")
      if (questionCardNode == null || !questionCardNode.isObject)
        throw new IllegalArgumentException("'questionCard' node not found")

      val objCard = questionCardNode.asInstanceOf[ObjectNode]
      objCard.put("collection_id", collectionId)

      val datasetQueryNode = objCard.get("dataset_query")
      if (datasetQueryNode == null || !datasetQueryNode.isObject)
        throw new IllegalArgumentException("'dataset_query' node not found")

      datasetQueryNode.asInstanceOf[ObjectNode].put("database", databaseId)

      jsonFile
    }

    /** Create question card and return ID */
    def createQuestionCard(json: JsonNode): Int = {
      try {
        val requestBody = json.get("questionCard")
        val response = metabaseUtil.createQuestionCard(requestBody.toString)
        val responseJson = objectMapper.readTree(response)
        val id = Option(responseJson.get("id")).map(_.asInt()).getOrElse(-1)
        println(s"✅ [SUCCESS] Created filter question. New ID = $id")
        id
      } catch {
        case ex: Exception =>
          println(s"❌ [ERROR] Failed creating filter: ${ex.getMessage}")
          -1
      }
    }

    // Run steps
    val replacedJson = replacePlaceholders(queryResult)
    val updatedJson = updateIds(replacedJson)
    val questionId = createQuestionCard(updatedJson)

    println(s"🎯 [DONE] Filter question created successfully for orgId=$orgId → ID=$questionId")
    questionId
  }

  def createMetadataFilterCard(
                                metabaseUtil: MetabaseUtil,
                                filterJson: JsonNode,
                                collectionId: Int,
                                orgId: Int
                              ): Int = {

    try {
      val wrapper: ObjectNode = JsonNodeFactory.instance.objectNode()
      val questionCard = filterJson.deepCopy().asInstanceOf[ObjectNode]

      // Set mandatory fields
      questionCard.put("collection_id", collectionId)
      questionCard.put("name", filterJson.path("name").asText(s"Filter-${System.currentTimeMillis()}"))
      questionCard.put("display", "filter")  // Mark it as a filter card

      // Optional: set org or other metadata if needed
      questionCard.put("org_id", orgId)

      wrapper.set("questionCard", questionCard)

      // Call Metabase API to create card
      val responseStr = metabaseUtil.createQuestionCard(wrapper.toString)
      val responseJson = objectMapper.readTree(responseStr)
      val cardId = responseJson.path("id").asInt(-1)

      if (cardId == -1) {
        println(s"[ERROR] Failed to create metadata filter card: $responseStr")
      } else {
        println(s"[INFO] Successfully created metadata filter card '${questionCard.path("name").asText()}' → card_id=$cardId")
      }

      cardId
    } catch {
      case ex: Exception =>
        println(s"[EXCEPTION] createMetadataFilterCard failed: ${ex.getMessage}")
        ex.printStackTrace()
        -1
    }
  }
}

