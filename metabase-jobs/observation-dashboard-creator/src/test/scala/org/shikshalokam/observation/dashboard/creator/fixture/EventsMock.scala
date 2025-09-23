package org.shikshalokam.observation.dashboard.creator.fixture

object EventsMock {

  val METABASE_DASHBOARD_EVENT_1: String = """{"reportType":"Observation","publishedAt":"2025-05-13 00:03:32","dashboardData":{"targetedProgram":"681c665ec0e630ae1d7b6679","targetedSolution":"681c665ec0e630ae1d7b445a","isRubric":"false","admin":"1"},"_id":"65c25f88-13a2-4c92-a864-d725e75a87ce"}"""

  val METABASE_DASHBOARD_EVENT_2: String = """{"reportType":"Observation","publishedAt":"2025-07-01 10:49:12","dashboardData":{"targetedProgram":"685c266901a317159266588b","targetedSolution":"685c267701a31715926658ef","isRubric":"false","entityType":"school"},"_id":"f96f0a8f-5f6b-46dd-853e-5e4cb0050b08"}"""

  val METABASE_DASHBOARD_EVENT_3: String = """{"reportType":"Observation","publishedAt":"2025-07-01 11:16:17","dashboardData":{"targetedProgram":"684fe6ab826b8c99acc38653","targetedSolution":"684fe872826b8c99acc38771","isRubric":"true","entityType":"cluster"},"_id":"1e76ee0c-1540-4e27-b4de-3ab9390306bd"}"""

  val CUSTOMIZED_FILTER_EVENT: String = """{"reportType":"Observation","publishedAt":"2025-09-22 09:28:00","dashboardData":{"targetedProgram":"688b43a7ab139a00141e1bed","targetedSolution":"688b43ab2df127c1bf2b0369","isRubric":"false","entityType":"Social Studies"},"_id":"65df04e3-296d-4885-b559-cc072b4629e3"}"""

  val MULTISOLUTION_EVENT: String = """{"reportType":"Observation","publishedAt":"2025-09-11 12:44:17","dashboardData":{"targetedProgram":"688b43a7ab139a00141e1bed","targetedSolution":"688b43ab2df127c1bf2b0369","isRubric":"false","entityType":"Social Studies"},"_id":"84b55572-3134-4906-b6d6-d5fade1f8d8d"}"""

  val UPDATE_FILTER_EVENT: String = """{"reportType":"Observation","publishedAt":"2025-09-22 11:44:46","dashboardData":{"filterSync":"Yes","targetedSolution":"688b43ab2df127c1bf2b0369","filterTable":"688b43ab2df127c1bf2b0369_status"},"_id":"59d39c99-d9b0-4769-af11-a39ba7f1a7f4"}"""

  val UPDATE_FILTER_EVENT2: String = """{"reportType":"Project","publishedAt":"2025-09-22 22:50:22","dashboardData":{"filterTable":"local_projects","filterSync":"Yes"},"_id":"79e2ad58-db0a-4f27-91e7-f84f4a25c1c5"}"""
}
