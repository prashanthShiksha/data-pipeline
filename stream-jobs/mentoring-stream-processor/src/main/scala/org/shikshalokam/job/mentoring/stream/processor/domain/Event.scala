package org.shikshalokam.job.mentoring.stream.processor.domain

import org.shikshalokam.job.domain.reader.JobRequest

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{Instant, OffsetDateTime}
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

class Event(eventMap: java.util.Map[String, Any], partition: Int, offset: Long)
  extends JobRequest(eventMap, partition, offset) {

  def eventType: String = readOrDefault[String]("eventType", null)

  def entity: String = readOrDefault[String]("entity", null)

  def name: String = readOrDefault[String]("name", null)

  def username: String = readOrDefault[String]("username", null)

  def status: String = readOrDefault[String]("status", null)

  def createdBy: String = extractValue[String]("created_by").orNull

  def updatedBy: String = readOrDefault[String]("updated_by", null)

  def id: Int = extractValue[Int]("id").getOrElse(-1)

  def tenantCode: String = extractValue[String]("tenant_code").orNull

  def createdAt: Timestamp = parseTimestamp(extractValue[Any]("created_at").orNull)

  def updatedAt: Timestamp = parseTimestamp(extractValue[Any]("updated_at").orNull)

  def deletedAt: Timestamp = parseTimestamp(extractValue[Any]("deleted_at").orNull)

  def isDeleted: Boolean = extractValue[Boolean]("deleted").getOrElse(false)

  def sessionId: Int = extractValue[Int]("session_id").getOrElse(-1)

  def mentorId: String = extractValue[String]("mentor_id").orNull

  def sessionName: String = extractValue[String]("name").orNull

  def sessionDes: String = extractValue[String]("description").orNull

  def sessionType: String = extractValue[String]("type").orNull

  def sessionStatus: String = extractValue[String]("status").orNull

  def startedAt: Timestamp = parseTimestamp(extractValue[Any]("started_at").orNull)

  def completedAt: Timestamp = parseTimestamp(extractValue[Any]("completed_at").orNull)

  def recommendedFor: String = extractCollectionAsCsv("recommended_for")

  def categories: String = extractCollectionAsCsv("categories")

  def medium: String = extractCollectionAsCsv("medium")

  def attId: Int = extractValue[Int]("att_id").getOrElse(-1)

  def atSessionId: Int = extractValue[Int]("session_id").getOrElse(-1)

  def menteeId: String = extractValue[String]("mentee_id").orNull

  def joinedAt: Timestamp = parseTimestamp(extractValue[Any]("joined_at").orNull)

  def leftAt: Timestamp = parseTimestamp(extractValue[Any]("left_at").orNull)

  def isFeedbackSkipped: Boolean = extractValue[Boolean]("is_feedback_skipped").getOrElse(false)

  def connId: Int = extractValue[Int]("conn_id").getOrElse(-1)

  def userId: String = extractValue[String]("user_id").orNull

  def friendId: String = extractValue[String]("friend_id").orNull

  def orgId: String = extractValue[String]("org_id").orNull

  def orgName: String = extractValue[String]("org_name").orNull

  def orgCode: String = extractValue[String]("org_code").orNull

  def platform: String = extractValue[String]("platform").orNull

  def rating: Int = extractValue[Int]("rating").getOrElse(-1)

  def ratingUpdatedAt: Timestamp = parseTimestamp(extractValue[Any]("rating_updated_at").orNull)

  def userIdRoles: Int = extractValue[Int]("userId").getOrElse(-1)

  def organizations: Seq[Map[String, Any]] = extractValue[Seq[Map[String, Any]]]("organizations").getOrElse(Seq.empty)

  private val isUpdateEvent: Boolean = eventType == "update" || eventType == "bulk-update"

  private def extractValue[T](key: String)(implicit ct: ClassTag[T]): Option[T] = {
    def readKey(k: String): Option[Any] = Option(readOrDefault[Any](k, null.asInstanceOf[Any]))

    val direct = readKey(key)
    val fromNew = if (isUpdateEvent) readKey(s"newValues.$key") else None
    val fromOld = if (isUpdateEvent) readKey(s"oldValues.$key") else None

    (direct orElse fromNew orElse fromOld)
      .filter(_ != null)
      .map(_.asInstanceOf[T])
  }

  private def extractCollectionAsCsv(key: String): String = {
    extractValue[AnyRef](key)
      .flatMap {
        case seq: Seq[_] => Some(seq.collect { case s: String => s })
        case coll: java.util.Collection[_] => Some(coll.asScala.collect { case s: String => s })
        case _ => None
      }
      .map(_.mkString(","))
      .getOrElse("")
  }

  private def parseTimestamp(value: Any): Timestamp = value match {
    case null => null
    case ts: Timestamp => ts
    case s: String if s.trim.nonEmpty =>
      val str = s.trim
      val parsers = Seq[(String => Option[Timestamp])](
        in => scala.util.Try(Timestamp.valueOf(in)).toOption, //SQL timestamp format
        in => scala.util.Try(Timestamp.from(Instant.parse(in))).toOption, //ISO-8601 UTC format
        in => scala.util.Try { //ISO-8601 with offset
          val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX")
          val odt = OffsetDateTime.parse(in, formatter)
          Timestamp.from(odt.toInstant)
        }.toOption,
        in => scala.util.Try { // Fallback format without millis or TZ
          val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
          new Timestamp(formatter.parse(in).getTime)
        }.toOption
      )
      parsers.view.flatMap(parser => parser(str)).headOption.orNull
    case _ => null
  }
}
