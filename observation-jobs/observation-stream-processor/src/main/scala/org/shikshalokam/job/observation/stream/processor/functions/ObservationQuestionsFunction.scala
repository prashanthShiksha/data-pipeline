package org.shikshalokam.job.observation.stream.processor.functions

import org.shikshalokam.job.observation.stream.processor.answers.ObservationStreamConfig
import org.shikshalokam.job.util.PostgresUtil

import scala.collection.immutable._

//class ObservationQuestionsFunction(postgresUtil: PostgresUtil, config: ObservationStreamConfig) {

//  def textQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String, user_id: String,
//                       submission_number: Integer, value: String, state_name: String, district_name: String, block_name: String, cluster_name: String,
//                       school_name: String, has_parent_question: String, parent_question_text: String): Unit = {
//
//    val question: String = payload.flatMap(_.get("question")) match {
//      case Some(qList: List[_]) =>
//        qList.collect { case q: String if q.nonEmpty => q }.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val labels: String = payload.flatMap(_.get("labels")) match {
//      case Some(labelList: List[_]) =>
//        labelList.collect{ case q: String if q.nonEmpty => q}.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val insertQuestionsQuery =
//      s"""INSERT INTO ${config.questions} (
//         |    solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name, org_name,
//         |    question_id, question_text, value, score, has_parent_question, parent_question_text, evidence,
//         |    submitted_at, remarks, question_type, labels
//         |) VALUES (
//         |   '$solution_id', '$submission_id', '$user_id', '$state_name', '$district_name', '$block_name', '$cluster_name', '$school_name', NULL,
//         |   '$question_id', '$question', '$value', NULL, '$has_parent_question', '$parent_question_text', NULL, NULL, NULL, 'text', '$labels'
//         |);
//         |""".stripMargin
//
//    postgresUtil.insertData(insertQuestionsQuery)
//  }
//
//  def radioQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String, user_id: String,
//                        submission_number: Integer, value: String, state_name: String, district_name: String, block_name: String, cluster_name: String,
//                        school_name: String, score: Integer, has_parent_question: String, parent_question_text: String): Unit = {
//
//    val question: String = payload.flatMap(_.get("question")) match {
//      case Some(qList: List[_]) =>
//        qList.collect { case q: String if q.nonEmpty => q }.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val labels: String = payload.flatMap(_.get("labels")) match {
//      case Some(labelList: List[_]) =>
//        labelList.collect{ case q: String if q.nonEmpty => q}.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val insertRadioTypeQuery =
//      s"""INSERT INTO ${config.questions} (
//         |    solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name, org_name,
//         |    question_id, question_text, value, score, has_parent_question, parent_question_text, evidence,
//         |    submitted_at, remarks, question_type, labels
//         |) VALUES (
//         |   '$solution_id', '$submission_id', '$user_id', '$state_name', '$district_name', '$block_name', '$cluster_name', '$school_name',NULL,
//         |   '$question_id', '$question', '$value', '$score', '$has_parent_question', '$parent_question_text', NULL, NULL, NULL, 'radio', '$labels'
//         |);
//         |""".stripMargin
//
//    postgresUtil.insertData(insertRadioTypeQuery)
//  }
//
//  def dateQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String, user_id: String,
//                       submission_number: Integer, value: String, state_name: String, district_name: String, block_name: String, cluster_name: String,
//                       school_name: String, score: Integer,has_parent_question: String, parent_question_text: String): Unit = {
//
//    val question: String = payload.flatMap(_.get("question")) match {
//      case Some(qList: List[_]) =>
//        qList.collect { case q: String if q.nonEmpty => q }.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val labels: String = payload.flatMap(_.get("labels")) match {
//      case Some(labelList: List[_]) =>
//        labelList.collect{ case q: String if q.nonEmpty => q}.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val insertRadioTypeQuery =
//      s"""INSERT INTO ${config.questions} (
//         |    solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name, org_name,
//         |    question_id, question_text, value, score, has_parent_question, parent_question_text, evidence,
//         |    submitted_at, remarks, question_type, labels
//         |) VALUES (
//         |   '$solution_id', '$submission_id', '$user_id', '$state_name', '$district_name', '$block_name', '$cluster_name', '$school_name', NULL,
//         |   '$question_id', '$question', '$value', '$score', '$has_parent_question', '$parent_question_text, NULL, NULL, NULL, 'date', '$labels'
//         |);
//         |""".stripMargin
//
//    postgresUtil.insertData(insertRadioTypeQuery)
//  }
//
//  def multiselectQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String, user_id: String,
//                              submission_number: Integer, value: String, state_name: String, district_name: String, block_name: String, cluster_name: String,
//                              school_name: String, has_parent_question: String, parent_question_text: String): Unit = {
//
//    val question: String = payload.flatMap(_.get("question")) match {
//      case Some(qList: List[_]) =>
//        qList.collect { case q: String if q.nonEmpty => q }.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val labels: String = payload.flatMap(_.get("labels")) match {
//      case Some(labelList: List[_]) =>
//        labelList.collect{ case q: String if q.nonEmpty => q}.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val insertQuestionsQuery =
//      s"""INSERT INTO ${config.questions} (
//         |    solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name, org_name,
//         |    question_id, question_text, value, score, has_parent_question, parent_question_text, evidence,
//         |    submitted_at, remarks, question_type, labels
//         |) VALUES (
//         |   '$solution_id', '$submission_id', '$user_id', '$state_name', '$district_name', '$block_name', '$cluster_name', '$school_name', NULL,
//         |   '$question_id', '$question', '$value', NULL, '$has_parent_question', '$parent_question_text', NULL, NULL, NULL, 'multiselect', '$labels'
//         |);
//         |""".stripMargin
//
//    postgresUtil.insertData(insertQuestionsQuery)
//  }
//
//  def numberQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String, user_id: String,
//                              submission_number: Integer, value: String, state_name: String, district_name: String, block_name: String, cluster_name: String,
//                              school_name: String, has_parent_question: String, parent_question_text: String): Unit = {
//
//    val question: String = payload.flatMap(_.get("question")) match {
//      case Some(qList: List[_]) =>
//        qList.collect { case q: String if q.nonEmpty => q }.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val labels: String = payload.flatMap(_.get("labels")) match {
//      case Some(labelList: List[_]) =>
//        labelList.collect { case q if q != null => q.toString }.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val insertQuestionsQuery =
//      s"""INSERT INTO ${config.questions} (
//         |    solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name, org_name,
//         |    question_id, question_text, value, score, has_parent_question, parent_question_text, evidence,
//         |    submitted_at, remarks, question_type, labels
//         |) VALUES (
//         |   '$solution_id', '$submission_id', '$user_id', '$state_name', '$district_name', '$block_name', '$cluster_name', '$school_name', NULL,
//         |   '$question_id', '$question', '$value', NULL, '$has_parent_question', '$parent_question_text', NULL, NULL, NULL, 'number', '$labels'
//         |);
//         |""".stripMargin
//
//    postgresUtil.insertData(insertQuestionsQuery)
//  }
//
//  def sliderQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String, user_id: String,
//                         submission_number: Integer, value: String, state_name: String, district_name: String, block_name: String, cluster_name: String,
//                         school_name: String, has_parent_question: String, parent_question_text: String): Unit = {
//
//    val question: String = payload.flatMap(_.get("question")) match {
//      case Some(qList: List[_]) =>
//        qList.collect { case q: String if q.nonEmpty => q }.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val labels: String = payload.flatMap(_.get("labels")) match {
//      case Some(labelList: List[_]) =>
//        labelList.collect{ case q: String if q.nonEmpty => q}.headOption.getOrElse("")
//      case _ => ""
//    }
//
//    val insertQuestionsQuery =
//      s"""INSERT INTO ${config.questions} (
//         |    solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name, org_name,
//         |    question_id, question_text, value, score, has_parent_question, parent_question_text, evidence,
//         |    submitted_at, remarks, question_type, labels
//         |) VALUES (
//         |   '$solution_id', '$submission_id', '$user_id', '$state_name', '$district_name', '$block_name', '$cluster_name', '$school_name', NULL,
//         |   '$question_id', '$question', '$value', NULL, '$has_parent_question', '$parent_question_text', NULL, NULL, NULL, 'slider', '$labels'
//         |);
//         |""".stripMargin
//
//    postgresUtil.insertData(insertQuestionsQuery)
//  }
//}
class ObservationQuestionsFunction(postgresUtil: PostgresUtil, config: ObservationStreamConfig, questionTable: String) {

  private def extractField(payload: Option[Map[String, Any]], key: String): String = {
    payload.flatMap(_.get(key)) match {
      case Some(qList: List[_]) =>
        qList.collect { case q if q != null => q.toString }.headOption.getOrElse("")
      case _ => ""
    }
  }

  private def insertQuestion(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String,
                              user_id: String, submission_number: Integer, value: String, state_name: String, district_name: String,
                              block_name: String, cluster_name: String, school_name: String, has_parent_question: String,
                              parent_question_text: String, question_type: String, score: Option[Integer] = None): Unit = {
    val question = extractField(payload, "question")
    val labels = extractField(payload, "labels")

    val scoreValue = score.map(_.toString).getOrElse("NULL")

    val insertQuestionQuery =
      s"""INSERT INTO $questionTable (
         |    solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name, org_name,
         |    question_id, question_text, value, score, has_parent_question, parent_question_text, evidence,
         |    submitted_at, remarks, question_type, labels
         |) VALUES (
         |   ?, ?, ?, ?', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
         |);
         |""".stripMargin

    val questionParam = Seq(
      solution_id, submission_id, user_id, state_name, district_name, block_name, cluster_name, school_name,
      question_id, question, value, scoreValue, has_parent_question, parent_question_text, null,
      null, null, question_type, labels
    )

    postgresUtil.executePreparedUpdate(insertQuestionQuery, questionParam, questionTable, solution_id)

  }

  def textQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String,
                        user_id: String, submission_number: Integer, value: String, state_name: String, district_name: String,
                        block_name: String, cluster_name: String, school_name: String, has_parent_question: String,
                        parent_question_text: String): Unit = {
    insertQuestion(payload, question_id, solution_id, submission_id, user_id, submission_number, value, state_name, district_name, block_name, cluster_name, school_name, has_parent_question, parent_question_text, "text")
  }

  def radioQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String,
                         user_id: String, submission_number: Integer, value: String, state_name: String, district_name: String,
                         block_name: String, cluster_name: String, school_name: String, score: Integer, has_parent_question: String,
                         parent_question_text: String): Unit = {
    insertQuestion(payload, question_id, solution_id, submission_id, user_id, submission_number, value, state_name, district_name, block_name, cluster_name, school_name, has_parent_question, parent_question_text, "radio", Some(score))
  }

  def dateQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String,
                        user_id: String, submission_number: Integer, value: String, state_name: String, district_name: String,
                        block_name: String, cluster_name: String, school_name: String, score: Integer, has_parent_question: String,
                        parent_question_text: String): Unit = {
    insertQuestion(payload, question_id, solution_id, submission_id, user_id, submission_number, value, state_name, district_name, block_name, cluster_name, school_name, has_parent_question, parent_question_text, "date", Some(score))
  }

  def multiselectQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String,
                               user_id: String, submission_number: Integer, value: String, state_name: String, district_name: String,
                               block_name: String, cluster_name: String, school_name: String, has_parent_question: String,
                               parent_question_text: String): Unit = {
    insertQuestion(payload, question_id, solution_id, submission_id, user_id, submission_number, value, state_name, district_name, block_name, cluster_name, school_name, has_parent_question, parent_question_text, "multiselect")
  }

  def numberQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String,
                          user_id: String, submission_number: Integer, value: String, state_name: String, district_name: String,
                          block_name: String, cluster_name: String, school_name: String, has_parent_question: String,
                          parent_question_text: String): Unit = {
    insertQuestion(payload, question_id, solution_id, submission_id, user_id, submission_number, value, state_name, district_name, block_name, cluster_name, school_name, has_parent_question, parent_question_text, "number")
  }

  def sliderQuestionType(payload: Option[Map[String, Any]], question_id: String, solution_id: String, submission_id: String,
                          user_id: String, submission_number: Integer, value: String, state_name: String, district_name: String,
                          block_name: String, cluster_name: String, school_name: String, has_parent_question: String,
                          parent_question_text: String): Unit = {
    insertQuestion(payload, question_id, solution_id, submission_id, user_id, submission_number, value, state_name, district_name, block_name, cluster_name, school_name, has_parent_question, parent_question_text, "slider")
  }
}