package org.shikshalokam.job.dashboard.creator.functions

import org.shikshalokam.job.util.{MetabaseUtil, PostgresUtil}
import org.shikshalokam.job.util.JSONUtil.mapper
import scala.collection.JavaConverters._

object CreateDashboard {
    def checkAndCreateCollection(collectionName: String,ReportType: String, metabaseUtil: MetabaseUtil, postgresUtil: PostgresUtil,metaTableQuery:String): Int = {
      val collectionListJson = mapper.readTree(metabaseUtil.listCollections())
      val existingCollectionId = collectionListJson.elements().asScala
        .find(_.path("name").asText() == collectionName)
        .map(_.path("id").asInt())
      println(s"existingCollectionId = $existingCollectionId")
      existingCollectionId match {
        case Some(id) =>
          val errorMessage = s"$collectionName already exists with ID: $id."
          val updateTableQuery = metaTableQuery.replace("'errorMessage'", s"'${errorMessage.replace("'", "''")}'")
          postgresUtil.insertData(updateTableQuery)
          throw new IllegalStateException(s"$errorMessage. Process stopped.")

        case None =>
          println(s"Collection '$collectionName' does not exist. Creating new collection.")
          val collectionRequestBody =
                  s"""{
                     |  "name": "$collectionName",
                     |  "description": "Collection for $ReportType reports"
                     |}""".stripMargin
          val collectionId = mapper.readTree(metabaseUtil.createCollection(collectionRequestBody)).path("id").asInt()
          println(s"New Collection ID = $collectionId")
          collectionId
      }
    }

    def checkAndCreateDashboard(collectionId: Int, dashboardName: String, metabaseUtil: MetabaseUtil, postgresUtil: PostgresUtil,metaTableQuery: String): Int = {
      val dashboardListJson = mapper.readTree(metabaseUtil.listDashboards())
      val existingDashboardId = dashboardListJson.elements().asScala
        .find(_.path("name").asText() == dashboardName)
        .map(_.path("id").asInt())
      println(s"existingDashboardId = $existingDashboardId")
      existingDashboardId match {
        case Some(id) =>
          val errorMessage = s"$dashboardName already exists with ID: $id."
          val updateTableQuery = metaTableQuery.replace("'errorMessage'", s"'${errorMessage.replace("'", "''")}'")
          postgresUtil.insertData(updateTableQuery)
          throw new IllegalStateException(s"$errorMessage. Process stopped.")

        case None =>
          println(s"Collection '$dashboardName' does not exist. Creating new dashboard.")
          val dashboardRequestBody = s"""{
             |  "name": "$dashboardName",
             |  "collection_id": "$collectionId"
             |}""".stripMargin
          val dashboardId = mapper.readTree(metabaseUtil.createDashboard(dashboardRequestBody)).path("id").asInt()
          println(s"New Dashboard ID = $dashboardId")
          dashboardId
      }
    }

    def getDatabaseId(metabaseDatabase: String, metabaseUtil: MetabaseUtil): Int = {
      val databaseListJson = mapper.readTree(metabaseUtil.listDatabaseDetails())
      val databaseId = databaseListJson.path("data").elements().asScala
        .find(_.path("name").asText() == metabaseDatabase)
        .map(_.path("id").asInt())
        .getOrElse(throw new IllegalStateException(s"Database '$metabaseDatabase' not found. Process stopped."))
      println(s"Database ID = $databaseId")
      databaseId
    }

    def getTableMetadataId(databaseId: Int, metabaseUtil: MetabaseUtil, tableName: String, columnName: String,postgresUtil: PostgresUtil,metaTableQuery: String): Int = {
      val metadataJson = mapper.readTree(metabaseUtil.getDatabaseMetadata(databaseId))
      metadataJson.path("tables").elements().asScala
        .find(_.path("name").asText() == s"$tableName")
        .flatMap(table => table.path("fields").elements().asScala
          .find(_.path("name").asText() == s"$columnName"))
        .map(field => {
          val fieldId = field.path("id").asInt()
          println(s"Field ID for $columnName: $fieldId")
          fieldId
        }).getOrElse {
          val errorMessage = s"$columnName field not found"
          val updateTableQuery = metaTableQuery.replace("'errorMessage'", s"'${errorMessage.replace("'", "''")}'")
          postgresUtil.insertData(updateTableQuery)
          throw new Exception(s"$columnName field not found")
        }
    }

}
