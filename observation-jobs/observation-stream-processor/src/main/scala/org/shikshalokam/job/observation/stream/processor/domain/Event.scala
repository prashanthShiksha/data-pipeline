package org.shikshalokam.job.observation.stream.processor.domain

import org.shikshalokam.job.domain.reader.JobRequest

class Event(eventMap: java.util.Map[String, Any], partition: Int, offset: Long) extends JobRequest(eventMap, partition, offset) {

  def _id: String = readOrDefault[String]("_id", "")

  def solutionId: String = readOrDefault[String]("solutionInfo._id", "")

  def createdBy: String = readOrDefault[String]("createdBy", "")

  def submissionNumber: String = readOrDefault[String]("submissionNumber", "")

  def stateName: String = readOrDefault[String]("userProfile.state.label", "")

  def districtName: String = readOrDefault[String]("userProfile.district.label", "")

  def blockName: String = readOrDefault[String]("userProfile.block.label", "")

  def clusterName: String = readOrDefault[String]("userProfile.cluster.label", "")

  def schoolName: String = readOrDefault[String]("userProfile.school.label", "")

  def themes: List[Map[String, Any]] = readOrDefault[List[Map[String, Any]]]("themes", null)

  def criteria: List[Map[String, Any]] = readOrDefault[List[Map[String, Any]]]("criteria", null)

}
