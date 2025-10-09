package org.shikshalokam.job.mentoring.dashboard.creator.functions

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import org.shikshalokam.job.util.{MetabaseUtil, PostgresUtil}

import scala.collection.JavaConverters._
import scala.collection.mutable

object UpdateParameters {

  private val objectMapper = new ObjectMapper()

  /**
   * Build a slug -> filterCardId map from Postgres config
   * Handles IDs as strings or integers
   */
  def buildFilterSlugMap(
                          postgresUtil: PostgresUtil,
                          filterConfigQuery: String
                        ): Map[String, String] = {  // Use String for uniformity
    val filterResults = postgresUtil.fetchData(filterConfigQuery)
    val slugToFilterId = mutable.Map[String, String]()

    println(s"[DEBUG] Fetched ${filterResults.size} filter rows from Postgres")

    for ((row, idx) <- filterResults.zipWithIndex) {
      try {
        val configStr = row.getOrElse("config", "[]").toString
        println(s"[DEBUG] Row $idx config string: $configStr")
        val configJson = objectMapper.readTree(configStr)

        configJson match {
          case arr if arr.isArray =>
            arr.elements().asScala.foreach { filterJson =>
              val slug = filterJson.path("slug").asText()
              val cardId = filterJson.path("id").asText()
              if (cardId.nonEmpty) slugToFilterId(slug) = cardId
            }
          case obj if obj.isObject =>
            val slug = obj.path("slug").asText()
            val cardId = obj.path("id").asText()
            if (cardId.nonEmpty) slugToFilterId(slug) = cardId
          case other =>
            println(s"[WARN] Row $idx has invalid config type: $other")
        }

      } catch {
        case ex: Exception =>
          println(s"[EXCEPTION] Error processing row $idx: ${ex.getMessage}")
          ex.printStackTrace()
      }
    }

    println(s"[INFO] Slug to FilterCardId Map: ${slugToFilterId.toMap}")
    slugToFilterId.toMap
  }

  /**
   * Update dashboard parameters: map existing filters to cards using slug
   * Pushes all cards to the dashboard
   */
  def updateDashboardParameters(
                                 metabaseUtil: MetabaseUtil,
                                 dashboardId: Int,
                                 questionCards: Seq[ObjectNode],
                                 slugToFilterId: Map[String, String]  // IDs as string
                               ): Unit = {

    println(s"[DEBUG] Updating dashboard parameters for ${questionCards.size} cards")

    val dashcardsArray: ArrayNode = objectMapper.createArrayNode()

    questionCards.foreach { cardJson =>
      val dashcard: ObjectNode = objectMapper.createObjectNode()
      dashcard.put("card_id", cardJson.path("id").asText())
      dashcard.put("col", cardJson.path("col").asInt(0))
      dashcard.put("row", cardJson.path("row").asInt(0))
      dashcard.put("size_x", cardJson.path("size_x").asInt(6))
      dashcard.put("size_y", cardJson.path("size_y").asInt(4))

      val paramMappingsArray: ArrayNode = objectMapper.createArrayNode()

      slugToFilterId.foreach { case (slug, filterId) =>
        val mapping = objectMapper.createObjectNode()
        mapping.put("parameter_id", filterId)
        mapping.put("card_id", cardJson.path("id").asText())

        val targetArray = objectMapper.createArrayNode()
        val templateTagArray = objectMapper.createArrayNode()
        templateTagArray.add(slug)
        targetArray.add("dimension")
        targetArray.add(templateTagArray)
        mapping.set("target", targetArray)

        println(s"[DEBUG] Mapping slug='$slug' parameter_id='$filterId' → card_id='${cardJson.path("id").asText()}'")

        paramMappingsArray.add(mapping)
      }

      dashcard.set("parameter_mappings", paramMappingsArray)
      dashcardsArray.add(dashcard)
    }

    val payload: ObjectNode = objectMapper.createObjectNode()
    payload.set("dashcards", dashcardsArray)

    val response = metabaseUtil.addQuestionCardToDashboard(dashboardId, payload.toString)
    println(s"✔ Successfully updated dashboard parameters for $dashboardId | Response: $response")
    UpdateParameters.pushQuestionCardsToDashboard(metabaseUtil, dashboardId, questionCards)
  }

  def pushQuestionCardsToDashboard(
                                    metabaseUtil: MetabaseUtil,
                                    dashboardId: Int,
                                    questionCards: Seq[ObjectNode]
                                  ): Unit = {

    println(s"[DEBUG] Pushing ${questionCards.size} question cards to dashboard $dashboardId")

    val dashcardsArray: ArrayNode = objectMapper.createArrayNode()

    questionCards.foreach { cardJson =>
      val dashcard: ObjectNode = objectMapper.createObjectNode()
      dashcard.put("card_id", cardJson.path("id").asText())
      dashcard.put("col", cardJson.path("col").asInt(0))
      dashcard.put("row", cardJson.path("row").asInt(0))
      dashcard.put("size_x", cardJson.path("size_x").asInt(6))
      dashcard.put("size_y", cardJson.path("size_y").asInt(4))

      // No parameter mappings here, just pushing cards
      dashcardsArray.add(dashcard)
      println(s"[DEBUG] Added card_id='${cardJson.path("id").asText()}' to dashboard payload")
    }

    val payload: ObjectNode = objectMapper.createObjectNode()
    payload.set("dashcards", dashcardsArray)

    val response = metabaseUtil.addQuestionCardToDashboard(dashboardId, payload.toString)
    println(s"✔ Successfully pushed ${questionCards.size} cards to dashboard $dashboardId | Response: $response")
  }

}