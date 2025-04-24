package org.shikshalokam.job.dashboard.creator.domain

import org.shikshalokam.job.domain.reader.JobRequest

class Event(eventMap: java.util.Map[String, Any], partition: Int, offset: Long) extends JobRequest(eventMap, partition, offset) {

  def _id: String = readOrDefault[String]("_id", "")

  def reportType: String = readOrDefault[String]("reportType", "")

  def admin: String = readOrDefault("dashboardData.admin", "")

  def targetedProgram: String = readOrDefault("dashboardData.targetedProgram", "")

  def targetedDistrict: String = readOrDefault("dashboardData.targetedDistrict", "")

  def targetedState: String = readOrDefault("dashboardData.targetedState", "")

  def solution_id: String = readOrDefault("solution_id", "")

  def chartType: List[String] = readOrDefault[List[String]]("chart_type", List.empty[String])

  def isRubric: String = readOrDefault("isRubric","false")

  def solutionName: String = readOrDefault("solutionName", "")

}
