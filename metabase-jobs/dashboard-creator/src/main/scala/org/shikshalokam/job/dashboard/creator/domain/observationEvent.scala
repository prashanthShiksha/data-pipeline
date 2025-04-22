package org.shikshalokam.job.dashboard.creator.domain

import org.shikshalokam.job.domain.reader.JobRequest

class observationEvent(eventMap: java.util.Map[String, Any], partition: Int, offset: Long) extends JobRequest(eventMap, partition, offset){

  def _id: String = readOrDefault[String]("_id", "")

  def reportType: String = readOrDefault[String]("reportType", "")

  def solution_id: String = readOrDefault("solution_id", "")
}
