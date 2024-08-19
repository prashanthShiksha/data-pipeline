package org.shikshalokam.project.fixture

object EventsMock {

  val PROJECT_EVENT_1: String = """{"certificate":{"templateUrl":"certificateTemplates/64a405ce39347f0008761a16/9bb884fc-8a56-4727-9522-25a7d5b8ea06_4-6-2023-1688470990741.svg","status":"active","criteria":{"validationText":"Complete validation message","expression":"C1&&C2","conditions":{"C1":{"validationText":"Submit your project.","expression":"C1","conditions":{"C1":{"scope":"project","key":"status","operator":"==","value":"submitted"}}},"C2":{"validationText":"Add 2  evidences at the project level","expression":"C1","conditions":{"C1":{"scope":"project","key":"attachments","function":"count","filter":{"key":"type","value":"all"},"operator":">=","value":2}}}}},"templateId":"64a405ce39347f0008761a16"},"userId":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","userRole":"HM,DEO","status":"submitted","isDeleted":false,"categories":[{"_id":"5fcfa9a2457d6055e33843f1","name":"Infrastructure","externalId":"infrastructure"},{"_id":"5fcfa9a2457d6055e33843f2","name":"Community","externalId":"community"},{"_id":"5fcfa9a2457d6055e33843f3","name":"Education Leader","externalId":"educationLeader"}],"createdBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","tasks":[{"_id":"db885d2e-947e-493c-ad62-d4c00dcfac29","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","isDeleted":false,"isDeletable":false,"taskSequence":["Task1-1688470987181-subtask-6"],"children":[{"_id":"a0cbfc5b-d132-460a-96e8-e7f9aa3c8bc8","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":false,"taskSequence":[],"children":[],"visibleIf":[{"operator":"===","_id":"db885d2e-947e-493c-ad62-d4c00dcfac29","value":"started"}],"hasSubTasks":false,"learningResources":[],"deleted":false,"type":"content","name":"Send an invite for SMC meeting to the teachers and the SMC members","externalId":"Task1-1688470987181-subtask-6","description":"","updatedAt":"2024-05-22T10:51:27.217Z","createdAt":"2023-07-04T11:43:08.123Z","parentId":"64a405cc7c3e91000af9c272","status":"completed","referenceId":"64a405cc7c3e91000af9c27f","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z"},{"_id":"9eaddb3f-c15d-4ef4-b7a5-a610bd7cffbc","status":"notStarted","name":"asdf","endDate":"","assignee":"","type":"simple","attachments":[],"startDate":"","isDeleted":true,"isDeletable":true,"externalId":"asdf","createdAt":"2024-05-09T09:58:14.704Z","updatedAt":"2024-05-22T10:51:27.217Z","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z","children":[]}],"visibleIf":[],"hasSubTasks":true,"learningResources":[{"name":"गूगल मीट का उपयोग कैसे करें ? | How to use Google meet","link":"https://staging.sunbirded.org/resources/play/collection/do_21367765813765734412979?contentType=TextBook","app":"Diksha","id":"do_21367765813765734412979?contentType=TextBook"},{"name":"गूगल मीट की विशेषताएँ | Features of Google meet","link":"https://staging.sunbirded.org/resources/play/collection/do_21367765813765734412979?contentType=TextBook","app":"Diksha","id":"do_21367765813765734412979?contentType=TextBook"}],"deleted":false,"type":"content","name":"Conduct a needs assessment of the school","externalId":"Task1-1688470987181","description":"","sequenceNumber":"1","updatedAt":"2024-05-22T10:51:27.217Z","createdAt":"2023-07-04T11:43:08.106Z","status":"completed","referenceId":"64a405cc7c3e91000af9c272","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z","attachments":[{"name":"1716373382818.jpg","type":"image/jpeg","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/3b133827-a36f-45db-8b96-1bf7b0126bdc/1716373382818.jpg"},{"name":"1716373392687.jpeg","type":"image/jpeg","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/3b133827-a36f-45db-8b96-1bf7b0126bdc/1716373392687.jpeg"},{"name":"1716373399275.mp4","type":"video/mp4","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/3b133827-a36f-45db-8b96-1bf7b0126bdc/1716373399275.mp4"},{"name":"1716373404695.pdf","type":"application/pdf","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/3b133827-a36f-45db-8b96-1bf7b0126bdc/1716373404695.pdf"}]},{"_id":"78e98a0f-bd75-444b-b211-dbccf606b5f5","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","isDeleted":false,"isDeletable":true,"taskSequence":["Task2-1688470987181-subtask-6"],"children":[{"_id":"89831093-4b88-40a8-9e49-c83ff847677a","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":true,"taskSequence":[],"children":[],"visibleIf":[{"operator":"===","_id":"78e98a0f-bd75-444b-b211-dbccf606b5f5","value":"started"}],"hasSubTasks":false,"learningResources":[],"deleted":false,"type":"content","name":"Identify the areas of improvement in the school to work on","externalId":"Task2-1688470987181-subtask-6","description":"","updatedAt":"2024-05-22T10:51:27.217Z","createdAt":"2023-07-04T11:43:08.130Z","parentId":"64a405cc7c3e91000af9c275","status":"completed","referenceId":"64a405cc7c3e91000af9c284","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z"}],"visibleIf":[],"hasSubTasks":true,"learningResources":[{"name":"Molecular Genetics - Practice-1","link":"https://staging.sunbirded.org/resources/play/collection/do_21367765813765734412979?contentType=TextBook","app":"Diksha","id":"do_21367765813765734412979?contentType=TextBook"}],"deleted":false,"type":"content","name":"Analyse the findings from the needs assessment","externalId":"Task2-1688470987181","description":"","sequenceNumber":"2","updatedAt":"2024-05-22T10:51:27.217Z","createdAt":"2023-07-04T11:43:08.110Z","status":"completed","referenceId":"64a405cc7c3e91000af9c275","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z"},{"_id":"1f615901-96f2-4cf5-ba42-35fc183b454c","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","isDeleted":false,"isDeletable":true,"taskSequence":["Task3-1688470987181-subtask-6"],"children":[{"_id":"9447029b-fb38-4ad7-832f-ec782d751e3b","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":true,"isDeletable":true,"taskSequence":[],"children":[],"visibleIf":[{"operator":"===","_id":"1f615901-96f2-4cf5-ba42-35fc183b454c","value":"started"}],"hasSubTasks":false,"learningResources":[],"deleted":false,"type":"simple","name":"Discuss and finalise the timeline against each area of improvement","externalId":"Task3-1688470987181-subtask-6","description":"","updatedAt":"2024-05-22T10:51:27.217Z","createdAt":"2023-07-04T11:43:08.137Z","parentId":"64a405cc7c3e91000af9c278","status":"completed","referenceId":"64a405cc7c3e91000af9c289","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z"}],"visibleIf":[],"hasSubTasks":true,"learningResources":[],"deleted":false,"type":"simple","name":"Analyse the findings from the needs assessment","externalId":"Task3-1688470987181","description":"","sequenceNumber":"3","updatedAt":"2024-05-22T10:51:27.217Z","createdAt":"2023-07-04T11:43:08.114Z","status":"completed","referenceId":"64a405cc7c3e91000af9c278","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z","attachments":[{"name":"1714381676880.pdf","type":"application/pdf","url":"https://sunbirdstagingpublic.blob.core.windows.net/samiksha/survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/f005d649-5ef7-4952-8898-b193d75e29cb/1714381676880.pdf?sv=2023-01-03&st=2024-05-22T10%3A22%3A20Z&se=2024-05-23T16%3A22%3A20Z&sr=b&sp=r&sig=zE1Y7ONA%2FYzf9KO%2F%2F8iUQKvhc0WobUTRlpWhGe3oX5Y%3D","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/f005d649-5ef7-4952-8898-b193d75e29cb/1714381676880.pdf"},{"name":"www.google.com","type":"link","isUploaded":false,"url":""}]},{"_id":"722d3710-6aaf-4582-a5da-44bb0fb49318","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","isDeleted":false,"isDeletable":false,"taskSequence":[],"children":[],"visibleIf":[],"hasSubTasks":false,"learningResources":[],"deleted":false,"type":"observation","solutionDetails":{"type":"observation","subType":"school","minNoOfSubmissionsRequired":"2.0","_id":"64a405155c81330008b83b0f","isReusable":false,"externalId":"8351ed06-1a5f-11ee-92d6-70a8d3c32ea4-OBSERVATION-TEMPLATE_CHILD","name":"Observation 3","programId":"64943a13955f600008e2a3b7","allowMultipleAssessemts":true,"isRubricDriven":false,"criteriaLevelReport":false},"name":"Send an invite for SMC meeting to the teachers and the SMC members","externalId":"Task4-1688470987181","description":"","sequenceNumber":"4","updatedAt":"2024-05-22T10:51:27.217Z","createdAt":"2023-07-04T11:43:08.118Z","status":"completed","referenceId":"64a405cc7c3e91000af9c27b","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z","observationInformation":{"entityId":"09b9d681-36c8-4a00-86aa-0fd354fee844","programId":"64943a13955f600008e2a3b7","observationId":"64abe48bc60fd10008692c2d","solutionId":"64a405155c81330008b83b0f"},"submissions":[{"_id":"64abe48cc60fd10008692c37","status":"completed","completedDate":"2024-05-22T10:48:02.397Z"},{"_id":"664dcd3b4c7c5800084463f4","status":"started","completedDate":""},{"_id":"664dcd8a4c7c580008446420","status":"completed","completedDate":"2024-05-22T10:50:32.242Z"}]},{"_id":"e9a5abe7-172c-4e2a-96b5-693656cc9e21","status":"notStarted","name":"wsfgthujk","endDate":"2024-04-25T17:29:00+05:30","assignee":"","type":"simple","attachments":[],"startDate":"","children":[],"isDeleted":true,"isDeletable":true,"externalId":"wsfgthujk","createdAt":"2024-04-24T06:07:46.905Z","updatedAt":"2024-05-08T06:06:08.698Z","isImportedFromLibrary":false,"syncedAt":"2024-05-08T06:06:08.698Z","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e"},{"_id":"357abcc8-0ce1-4c7f-af8f-8da0484fbf51","status":"notStarted","name":"fgchjbkm","endDate":"2024-04-02T17:52:00+05:30","assignee":"","type":"simple","attachments":[{"name":"1714047628748.jpeg","type":"image/jpeg","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/da00e79d-3a7d-4eb0-9629-b4184fc8599a/1714047628748.jpeg","localUrl":"https://localhost/_capacitor_file_/storage/emulated/0/Android/data/org.sunbird.app/files/1714047628748.jpeg"},{"name":"1714047639700.jpg","type":"image/jpeg","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/da00e79d-3a7d-4eb0-9629-b4184fc8599a/1714047639700.jpg","localUrl":"https://localhost/_capacitor_file_/storage/emulated/0/Android/data/org.sunbird.app/files/1714047639700.jpg"},{"name":"1714047758795.pdf","type":"application/pdf","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/da00e79d-3a7d-4eb0-9629-b4184fc8599a/1714047758795.pdf"}],"startDate":"","children":[],"isDeleted":true,"isDeletable":true,"externalId":"fgchjbkm","createdAt":"2024-04-29T09:31:31.567Z","updatedAt":"2024-04-29T09:31:31.567Z","isImportedFromLibrary":false,"syncedAt":"2024-04-29T09:31:31.567Z","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e"},{"_id":"8c8de743-46ce-4f40-ab8b-194e4d26a6f6","status":"completed","name":"aSZDxfcgbh","endDate":"2024-04-18T14:38:00+05:30","assignee":"","type":"simple","attachments":[],"startDate":"","children":[],"isDeleted":false,"isDeletable":true,"externalId":"aszdxfcgbhjn","createdAt":"2024-05-06T06:16:55.859Z","updatedAt":"2024-05-22T10:51:27.217Z","isImportedFromLibrary":false,"syncedAt":"2024-05-22T10:51:27.217Z","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e"}],"updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","learningResources":[{"name":"Effective Parent Teacher Meetings | प्रभावी अभिभावक-शिक्षक बैठक","link":"https://staging.sunbirded.org/resources/play/collection/do_21367765813765734412979?contentType=TextBook","app":"Diksha","id":"do_21367765813765734412979?contentType=TextBook","isChecked":true},{"name":"     ధ్వని ","id":"do_31273040551886848011957","isChecked":true},{"name":"\tExamprep_10EM_ps_cha1_Q3","id":"do_31268582767737241615189","isChecked":true},{"name":"\" பாதுகாப்பு விதிமுறைகள்\"","id":"do_31256135038798233617237","isChecked":true},{"name":"\"कसरत अंको की \" का कार्यपत्रक","id":"do_312727086519074816126061","isChecked":true},{"name":"\tExamprep_10tm_ps_cha 11-Q3","id":"do_31269108472948326417493","isChecked":true}],"hasAcceptedTAndC":false,"taskSequence":["Task1-1688470987181","Task2-1688470987181","Task3-1688470987181","Task4-1688470987181"],"recommendedFor":[{"roleId":"5f32d8238e0dc831240405a0","code":"HM"}],"attachments":[{"name":"www.google.com","type":"link","isUploaded":false,"url":""},{"name":"1716375068018.jpeg","type":"image/jpeg","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/3b133827-a36f-45db-8b96-1bf7b0126bdc/1716375068018.jpeg"},{"name":"1716375078858.mp4","type":"video/mp4","url":"","sourcePath":"survey/64abe47e7c3e91000af9e474/c2e7fc3f-f364-45ae-8bad-0b41459b9e7e/3b133827-a36f-45db-8b96-1bf7b0126bdc/1716375078858.mp4"}],"deleted":false,"_id":"64abe47e7c3e91000af9e474","description":"Leveraging the huge number of private schools to show the significance of the financial problem by creating a petition and presenting to the authorities.","title":"Keep Our Schools Alive (Petition) - With Certificate 2","metaInformation":{"goal":"","rationale":"","primaryAudience":"","duration":"2 months","successIndicators":"","risks":"","approaches":""},"updatedAt":"2024-05-22T10:51:27.218Z","createdAt":"2023-07-10T10:59:07.091Z","__v":0,"solutionId":"64a405cc39347f00087619ff","solutionExternalId":"IDEAIMP-4-1688470987181-PROJECT-SOLUTION","programId":"64943a13955f600008e2a3b7","programExternalId":"Pgm_Certificate_Test_Program_6.0_QA","projectTemplateId":"64a405cc7c3e91000af9c296","projectTemplateExternalId":"IDEAIMP-4-1688470987181_IMPORTED","taskReport":{"total":5,"completed":5},"isAPrivateProgram":false,"programInformation":{"_id":"64943a13955f600008e2a3b7","externalId":"Pgm_Certificate_Test_Program_6.0_QA","description":"Certificate Test Program 6.0","name":"Certificate Test Program 6.0"},"solutionInformation":{"_id":"64a405cc39347f00087619ff","externalId":"IDEAIMP-4-1688470987181-PROJECT-SOLUTION","description":"Leveraging the huge number of private schools to show the significance of the financial problem by creating a petition and presenting to the authorities.","name":"Keep Our Schools Alive (Petition) - With Certificate 2"},"appInformation":{"appName":"Sunbird","appVersion":"6.0.local.0-debug"},"entityInformation":{"name":"BHARATAAMATHA MPL PS","externalId":"28222590105","hierarchy":[{"identifier":"d2f2f4db-246b-44e1-b877-05449ca16aec","code":"282225","name":"Anantapur","id":"d2f2f4db-246b-44e1-b877-05449ca16aec","type":"block","parentId":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03"},{"identifier":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03","code":"2822","name":"Ananthapuram","id":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03","type":"district","parentId":"bc75cc99-9205-463e-a722-5326857838f8"},{"code":"28","name":"Andhra Pradesh","id":"bc75cc99-9205-463e-a722-5326857838f8","type":"state"}],"_id":"09b9d681-36c8-4a00-86aa-0fd354fee844","entityType":"school","registryDetails":{"locationId":"09b9d681-36c8-4a00-86aa-0fd354fee844","code":"28222590105"}},"entityId":"09b9d681-36c8-4a00-86aa-0fd354fee844","lastDownloadedAt":"2023-07-10T10:59:07.157Z","userProfile":{"maskedPhone":null,"tcStatus":null,"channel":"dikshapreprodcustodian","profileUserTypes":[{"type":"administrator","subType":"hm"},{"type":"administrator","subType":"deo"}],"updatedDate":"2023-07-07 14:15:55:452+0000","managedBy":null,"flagsValue":0,"id":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","recoveryEmail":"","identifier":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","externalIds":[],"roleList":[{"name":"Book Creator","id":"BOOK_CREATOR"},{"name":"Membership Management","id":"MEMBERSHIP_MANAGEMENT"},{"name":"Flag Reviewer","id":"FLAG_REVIEWER"},{"name":"Report Viewer","id":"REPORT_VIEWER"},{"name":"Program Manager","id":"PROGRAM_MANAGER"},{"name":"Program Designer","id":"PROGRAM_DESIGNER"},{"name":"System Administration","id":"SYSTEM_ADMINISTRATION"},{"name":"Content Curation","id":"CONTENT_CURATION"},{"name":"Book Reviewer","id":"BOOK_REVIEWER"},{"name":"Content Creator","id":"CONTENT_CREATOR"},{"name":"Org Management","id":"ORG_MANAGEMENT"},{"name":"Course Admin","id":"COURSE_ADMIN"},{"name":"Org Moderator","id":"ORG_MODERATOR"},{"name":"Public","id":"PUBLIC"},{"name":"Admin","id":"ADMIN"},{"name":"Course Mentor","id":"COURSE_MENTOR"},{"name":"Content Reviewer","id":"CONTENT_REVIEWER"},{"name":"Report Admin","id":"REPORT_ADMIN"},{"name":"Org Admin","id":"ORG_ADMIN"}],"rootOrgId":"0126796199493140480","prevUsedEmail":"","firstName":"ft2","isMinor":false,"tncAcceptedOn":1688129418926,"profileDetails":null,"phone":"","dob":"1993-12-31","status":1,"lastName":"","tncLatestVersion":"v13","roles":[],"prevUsedPhone":"","stateValidated":false,"isDeleted":false,"organisations":[{"organisationId":"0126796199493140480","approvedBy":null,"channel":"dikshapreprodcustodian","updatedDate":null,"approvaldate":null,"isSystemUpload":false,"isDeleted":false,"id":"013829157164400640774","isApproved":null,"orgjoindate":"2023-06-30 12:50:14:031+0000","isSelfDeclaration":true,"updatedBy":null,"orgName":"Staging Custodian Organization","addedByName":null,"addedBy":null,"associationType":2,"locationIds":["027f81d8-0a2c-4fc6-96ac-59fe4cea3abf","8250d58d-f1a2-4397-bfd3-b2e688ba7141"],"orgLocation":[{"type":"state","id":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf"},{"type":"district","id":"8250d58d-f1a2-4397-bfd3-b2e688ba7141"}],"externalId":"101010","userId":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","isSchool":false,"hashTagId":"0126796199493140480","isSSO":false,"isRejected":null,"locations":[{"code":"29","name":"Karnataka","id":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf","type":"state","parentId":null},{"code":"2901","name":"BELAGAVI","id":"8250d58d-f1a2-4397-bfd3-b2e688ba7141","type":"district","parentId":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf"}],"position":null,"orgLeftDate":null},{"organisationId":"01275629914368409634323","approvedBy":null,"channel":"apekx","updatedDate":"2023-07-07 14:15:55:463+0000","approvaldate":null,"isSystemUpload":false,"isDeleted":false,"id":"01275629914368409634323","isApproved":false,"orgjoindate":"2023-06-30 12:51:34:939+0000","isSelfDeclaration":true,"updatedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","orgName":"BHARATAAMATHA MPL PS","addedByName":null,"addedBy":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","associationType":2,"locationIds":["bc75cc99-9205-463e-a722-5326857838f8","2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03","d2f2f4db-246b-44e1-b877-05449ca16aec"],"orgLocation":[{"type":"state","id":"bc75cc99-9205-463e-a722-5326857838f8"},{"type":"district","id":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03"},{"type":"block","id":"d2f2f4db-246b-44e1-b877-05449ca16aec"}],"externalId":"28222590105","userId":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","isSchool":true,"hashTagId":"01275629914368409634323","isSSO":false,"isRejected":false,"locations":[{"code":"28","name":"Andhra Pradesh","id":"bc75cc99-9205-463e-a722-5326857838f8","type":"state","parentId":null},{"code":"2822","name":"Ananthapuram","id":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03","type":"district","parentId":"bc75cc99-9205-463e-a722-5326857838f8"},{"code":"282225","name":"Anantapur","id":"d2f2f4db-246b-44e1-b877-05449ca16aec","type":"block","parentId":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03"}],"position":null,"orgLeftDate":null}],"provider":null,"countryCode":null,"tncLatestVersionUrl":"https://obj.stage.sunbirded.org/privacy-policy/terms-of-use.html","maskedEmail":"ft*@yopmail.com","email":"ft*@yopmail.com","rootOrg":{"organisationSubType":null,"channel":"dikshapreprodcustodian","description":"Pre-prod Custodian Organization","updatedDate":"2022-02-18 09:50:42:752+0000","organisationType":5,"isTenant":true,"provider":null,"id":"0126796199493140480","isBoard":true,"email":null,"slug":"dikshapreprodcustodian","isSSOEnabled":null,"orgName":"Staging Custodian Organization","updatedBy":null,"locationIds":["027f81d8-0a2c-4fc6-96ac-59fe4cea3abf","8250d58d-f1a2-4397-bfd3-b2e688ba7141"],"externalId":"101010","orgLocation":[{"type":"state","id":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf"},{"type":"district","id":"8250d58d-f1a2-4397-bfd3-b2e688ba7141"}],"isRootOrg":true,"rootOrgId":"0126796199493140480","imgUrl":null,"homeUrl":null,"createdDate":"2019-01-18 09:48:13:428+0000","createdBy":"system","hashTagId":"0126796199493140480","status":1},"tcUpdatedDate":null,"userLocations":[{"code":"2822","name":"Ananthapuram","id":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03","type":"district","parentId":"bc75cc99-9205-463e-a722-5326857838f8"},{"code":"28","name":"Andhra Pradesh","id":"bc75cc99-9205-463e-a722-5326857838f8","type":"state","parentId":null},{"code":"282225","name":"Anantapur","id":"d2f2f4db-246b-44e1-b877-05449ca16aec","type":"block","parentId":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03"},{"code":"28222590105","name":"BHARATAAMATHA MPL PS","id":"01275629914368409634323","type":"school","parentId":""}],"recoveryPhone":"","userName":"ft2_0as6","userId":"c2e7fc3f-f364-45ae-8bad-0b41459b9e7e","declarations":[],"promptTnC":false,"lastLoginTime":0,"createdDate":"2023-06-30 12:50:14:011+0000","framework":{"board":["CBSE"],"gradeLevel":["Class 1"],"id":["ekstep_ncert_k-12"],"medium":["English"],"subject":[]},"createdBy":null,"profileUserType":{"subType":"hm","type":"administrator"},"tncAcceptedVersion":"v13"},"userRoleInformation":{"district":"2f76dcf5-e43b-4f71-a3f2-c8f19e1fce03","state":"bc75cc99-9205-463e-a722-5326857838f8","block":"d2f2f4db-246b-44e1-b877-05449ca16aec","school":"28222590105","role":"HM,DEO"},"syncedAt":"2024-05-22T10:51:27.216Z","endDate":"2024-05-13T09:27:00.000Z","startDate":"2024-05-10T09:26:00.000Z","completedDate":"2024-05-22T10:51:27.217Z","remarks":"project level remarks "}"""

  val PROJECT_EVENT_2: String = """{"certificate":{"templateUrl":"certificateTemplates/664597c6a8861d0008d079c7/9bb884fc-8a56-4727-9522-25a7d5b8ea06_16-4-2024-1715836870730.svg","status":"active","criteria":{"validationText":"Complete validation message","expression":"C1&&C2&&C3&&C4","conditions":{"C1":{"validationText":"Submit your project.","expression":"C1","conditions":{"C1":{"scope":"project","key":"status","operator":"==","value":"submitted"}}},"C2":{"validationText":["Add 2 evidence at the project level"],"expression":"C1","conditions":{"C1":{"scope":"project","key":"attachments","function":"count","filter":{"key":"type","value":"all"},"operator":">=","value":2}}},"C3":{"validationText":"Add 3 evidence for the task Task - 1","expression":"C1","conditions":{"C1":{"scope":"task","key":"attachments","function":"count","filter":{"key":"type","value":"all"},"operator":">=","value":3,"taskDetails":["664597c5520d450008558a85"]}}},"C4":{"validationText":"Add 4 evidence for the task Task - 2","expression":"C1","conditions":{"C1":{"scope":"task","key":"attachments","function":"count","filter":{"key":"type","value":"all"},"operator":">=","value":4,"taskDetails":["664597c5520d450008558a88"]}}}}},"templateId":"664597c6a8861d0008d079c7"},"userId":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","userRole":"HM","status":"started","isDeleted":false,"categories":[{"_id":"5fcfa9a2457d6055e33843f0","externalId":"students","name":"Students"},{"_id":"5fcfa9a2457d6055e33843f3","externalId":"educationLeader","name":"Education Leader"}],"createdBy":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","tasks":[{"_id":"ed51bbc5-b0e3-4799-8578-31970bd3994f","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":false,"taskSequence":["Task1-1715836869000Task1"],"children":[{"_id":"bdd441d2-a00a-4a38-b7c0-413e4b1830a1","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":false,"taskSequence":[],"children":[],"visibleIf":[{"operator":"===","_id":"ed51bbc5-b0e3-4799-8578-31970bd3994f","value":"started"}],"hasSubTasks":false,"learningResources":[],"deleted":false,"type":"content","name":"Automation SubTask - 1","externalId":"Task1-1715836869000Task1","description":"","updatedAt":"2024-05-24T06:04:29.633Z","createdAt":"2024-05-16T05:21:09.079Z","parentId":"664597c5520d450008558a85","status":"notStarted","referenceId":"664597c5520d450008558a91","isImportedFromLibrary":false,"syncedAt":"2024-05-24T06:04:29.633Z"}],"visibleIf":[],"hasSubTasks":true,"learningResources":[{"name":"learn","link":"https://staging.sunbirded.org/play/content/do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content","app":"Diksha","id":"do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content"}],"deleted":false,"type":"content","name":"Task - 1","externalId":"Task1-1715836869000","description":"","sequenceNumber":"1","updatedAt":"2024-05-24T06:04:29.633Z","createdAt":"2024-05-16T05:21:09.028Z","status":"notStarted","referenceId":"664597c5520d450008558a85","isImportedFromLibrary":false,"syncedAt":"2024-05-24T06:04:29.633Z"},{"_id":"16481167-26dd-40be-b319-e457036cfc6e","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":true,"taskSequence":["Task2-1715836869000Task2"],"children":[{"_id":"9be81505-580c-4e0e-815a-f04cf9c27bfd","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":true,"taskSequence":[],"children":[],"visibleIf":[{"operator":"===","_id":"16481167-26dd-40be-b319-e457036cfc6e","value":"started"}],"hasSubTasks":false,"learningResources":[],"deleted":false,"type":"simple","name":"Automation SubTask - 2","externalId":"Task2-1715836869000Task2","description":"","updatedAt":"2024-05-24T06:04:29.633Z","createdAt":"2024-05-16T05:21:09.090Z","parentId":"664597c5520d450008558a88","status":"notStarted","referenceId":"664597c5520d450008558a96","isImportedFromLibrary":false,"syncedAt":"2024-05-24T06:04:29.633Z"}],"visibleIf":[],"hasSubTasks":true,"learningResources":[],"deleted":false,"type":"simple","name":"Task - 2","externalId":"Task2-1715836869000","description":"","sequenceNumber":"2","updatedAt":"2024-05-24T06:04:29.633Z","createdAt":"2024-05-16T05:21:09.065Z","status":"notStarted","referenceId":"664597c5520d450008558a88","isImportedFromLibrary":false,"syncedAt":"2024-05-24T06:04:29.633Z"},{"_id":"27351a44-2141-47b2-9084-815b89485f03","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":false,"taskSequence":[],"children":[],"visibleIf":[],"hasSubTasks":false,"learningResources":[],"deleted":false,"type":"simple","name":"Task - 3","externalId":"Task3-1715836869000","description":"","sequenceNumber":"3","updatedAt":"2024-05-24T06:04:29.633Z","createdAt":"2024-05-16T05:21:09.069Z","status":"notStarted","referenceId":"664597c5520d450008558a8b","isImportedFromLibrary":false,"syncedAt":"2024-05-24T06:04:29.633Z"},{"_id":"9023f1f2-fd3d-4f57-9e7e-1708e0f5f844","createdBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","updatedBy":"9bb884fc-8a56-4727-9522-25a7d5b8ea06","isDeleted":false,"isDeletable":false,"taskSequence":[],"children":[],"visibleIf":[],"hasSubTasks":false,"learningResources":[{"name":"learn","link":"https://staging.sunbirded.org/play/content/do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content","app":"Diksha","id":"do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content"}],"deleted":false,"type":"content","name":"Task - 4","externalId":"Task4-1715836869000","description":"","sequenceNumber":"4","updatedAt":"2024-05-24T06:04:29.633Z","createdAt":"2024-05-16T05:21:09.074Z","status":"notStarted","referenceId":"664597c5520d450008558a8e","isImportedFromLibrary":false,"syncedAt":"2024-05-24T06:04:29.633Z"}],"updatedBy":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","learningResources":[{"name":"Learning Resource -1","link":"https://staging.sunbirded.org/play/content/do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content","app":"Diksha","id":"do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content"},{"name":"https://staging.sunbirded.org/play/content/do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content","link":"https://staging.sunbirded.org/play/content/do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content","app":"Diksha","id":"do_31268582767737241615189?referrer=utm_source%3Dmobile%26utm_campaign%3Dshare_content"}],"hasAcceptedTAndC":false,"taskSequence":["Task1-1715836869000","Task2-1715836869000","Task3-1715836869000","Task4-1715836869000"],"recommendedFor":[{"roleId":"5f32d8238e0dc831240405a0","code":"HM"}],"attachments":[],"_id":"66502dedb8256f00086826ca","deleted":false,"description":"Leveraging the huge number of private schools to show the significance of the financial problem by creating a petition and presenting to the authorities.","title":"Test Project -7.0 AD","metaInformation":{"goal":"TEMP","rationale":"","primaryAudience":"","duration":"24 months","successIndicators":"","risks":"","approaches":""},"updatedAt":"2024-05-24T06:04:29.771Z","createdAt":"2024-05-24T06:04:29.629Z","__v":0,"solutionId":"664597c5a8861d0008d079b0","solutionExternalId":"IDEAIMP-4-1715836869000-PROJECT-SOLUTION","programId":"66459791a8861d0008d07962","programExternalId":"pgm_Regression_17_05_24","projectTemplateId":"664597c5520d450008558aa1","projectTemplateExternalId":"IDEAIMP-4-1715836869000_IMPORTED","taskReport":{"total":4,"notStarted":4},"isAPrivateProgram":false,"programInformation":{"_id":"66459791a8861d0008d07962","externalId":"pgm_Regression_17_05_24","description":"Automated testing is the application of software tools to automate. ସ୍ୱୟଂଚାଳିତ ପରୀକ୍ଷଣ","name":"Regression 7.0 AD test"},"solutionInformation":{"_id":"664597c5a8861d0008d079b0","externalId":"IDEAIMP-4-1715836869000-PROJECT-SOLUTION","description":"Leveraging the huge number of private schools to show the significance of the financial problem by creating a petition and presenting to the authorities.","name":"Test Project -7.0 AD"},"appInformation":{"appName":"Sunbird","appVersion":"6.0"},"entityInformation":{"name":"APTWRS ADDATEEGALA","externalId":"28140306106","hierarchy":[{"identifier":"e5be5e9c-3eea-4822-8754-9009c47c6782","code":"281403","name":"Addateegala","id":"e5be5e9c-3eea-4822-8754-9009c47c6782","type":"block","parentId":"24c36610-0640-45a3-b88e-fa92c9ebbec2"},{"identifier":"24c36610-0640-45a3-b88e-fa92c9ebbec2","code":"2830","name":"Alluri Sita Rama Raju","id":"24c36610-0640-45a3-b88e-fa92c9ebbec2","type":"district","parentId":"bc75cc99-9205-463e-a722-5326857838f8"},{"code":"28","name":"Andhra Pradesh","id":"bc75cc99-9205-463e-a722-5326857838f8","type":"state"}],"_id":"493590d5-ab64-4e2d-8b0a-c2820cb86020","entityType":"school","registryDetails":{"locationId":"493590d5-ab64-4e2d-8b0a-c2820cb86020","code":"28140306106"}},"entityId":"493590d5-ab64-4e2d-8b0a-c2820cb86020","lastDownloadedAt":"2024-05-24T06:04:29.731Z","userProfile":{"tcStatus":null,"channel":"dikshapreprodcustodian","profileUserTypes":[{"type":"administrator","subType":"hm"}],"updatedDate":"2024-05-24 06:03:56:460+0000","managedBy":null,"flagsValue":0,"id":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","identifier":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","updatedBy":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","externalIds":[],"roleList":[{"name":"Book Creator","id":"BOOK_CREATOR"},{"name":"Membership Management","id":"MEMBERSHIP_MANAGEMENT"},{"name":"Flag Reviewer","id":"FLAG_REVIEWER"},{"name":"Report Viewer","id":"REPORT_VIEWER"},{"name":"Program Manager","id":"PROGRAM_MANAGER"},{"name":"Program Designer","id":"PROGRAM_DESIGNER"},{"name":"System Administration","id":"SYSTEM_ADMINISTRATION"},{"name":"Content Curation","id":"CONTENT_CURATION"},{"name":"Book Reviewer","id":"BOOK_REVIEWER"},{"name":"Content Creator","id":"CONTENT_CREATOR"},{"name":"Org Management","id":"ORG_MANAGEMENT"},{"name":"Course Admin","id":"COURSE_ADMIN"},{"name":"Org Moderator","id":"ORG_MODERATOR"},{"name":"Public","id":"PUBLIC"},{"name":"Admin","id":"ADMIN"},{"name":"Course Mentor","id":"COURSE_MENTOR"},{"name":"Content Reviewer","id":"CONTENT_REVIEWER"},{"name":"Report Admin","id":"REPORT_ADMIN"},{"name":"Org Admin","id":"ORG_ADMIN"}],"rootOrgId":"0126796199493140480","firstName":"Nirmala","isMinor":true,"tncAcceptedOn":1716529698843,"profileDetails":null,"dob":"2009-12-31","status":1,"lastName":"","tncLatestVersion":"v13","roles":[],"stateValidated":false,"isDeleted":false,"organisations":[{"organisationId":"0126796199493140480","approvedBy":null,"channel":"dikshapreprodcustodian","updatedDate":null,"approvaldate":null,"isSystemUpload":false,"isDeleted":false,"id":"0140618148290396162","isApproved":null,"orgjoindate":"2024-05-24 05:48:14:563+0000","isSelfDeclaration":true,"updatedBy":null,"orgName":"Staging Custodian Organization","addedByName":null,"addedBy":null,"associationType":2,"locationIds":["027f81d8-0a2c-4fc6-96ac-59fe4cea3abf","8250d58d-f1a2-4397-bfd3-b2e688ba7141"],"orgLocation":[{"type":"state","id":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf"},{"type":"district","id":"8250d58d-f1a2-4397-bfd3-b2e688ba7141"}],"externalId":"101010","userId":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","isSchool":false,"hashTagId":"0126796199493140480","isSSO":false,"isRejected":null,"locations":[{"code":"29","name":"Karnataka","id":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf","type":"state","parentId":null},{"code":"2901","name":"BELAGAVI","id":"8250d58d-f1a2-4397-bfd3-b2e688ba7141","type":"district","parentId":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf"}],"position":null,"orgLeftDate":null},{"organisationId":"01274200593719296013305","approvedBy":null,"channel":"apekx","updatedDate":null,"approvaldate":null,"isSystemUpload":false,"isDeleted":false,"id":"0140618197629255683","isApproved":false,"orgjoindate":"2024-05-24 06:03:56:471+0000","isSelfDeclaration":true,"updatedBy":null,"orgName":"APTWRS ADDATEEGALA","addedByName":null,"addedBy":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","associationType":2,"locationIds":["bc75cc99-9205-463e-a722-5326857838f8","aecac7ab-15e4-45c9-ac7b-d716444cd652","e5be5e9c-3eea-4822-8754-9009c47c6782"],"orgLocation":[{"type":"state","id":"bc75cc99-9205-463e-a722-5326857838f8"},{"type":"district","id":"aecac7ab-15e4-45c9-ac7b-d716444cd652"},{"type":"block","id":"e5be5e9c-3eea-4822-8754-9009c47c6782"}],"externalId":"28140306106","userId":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","isSchool":true,"hashTagId":"01274200593719296013305","isSSO":false,"isRejected":false,"locations":[{"code":"28","name":"Andhra Pradesh","id":"bc75cc99-9205-463e-a722-5326857838f8","type":"state","parentId":null},{"code":"2814","name":"East Godavari","id":"aecac7ab-15e4-45c9-ac7b-d716444cd652","type":"district","parentId":"bc75cc99-9205-463e-a722-5326857838f8"},{"code":"281403","name":"Addateegala","id":"e5be5e9c-3eea-4822-8754-9009c47c6782","type":"block","parentId":"24c36610-0640-45a3-b88e-fa92c9ebbec2"}],"position":null,"orgLeftDate":null}],"provider":null,"countryCode":null,"tncLatestVersionUrl":"https://obj.stage.sunbirded.org/privacy-policy/terms-of-use.html","rootOrg":{"organisationSubType":null,"channel":"dikshapreprodcustodian","description":"Pre-prod Custodian Organization","updatedDate":"2022-02-18 09:50:42:752+0000","organisationType":5,"isTenant":true,"provider":null,"id":"0126796199493140480","isBoard":true,"email":null,"slug":"dikshapreprodcustodian","isSSOEnabled":null,"orgName":"Staging Custodian Organization","updatedBy":null,"locationIds":["027f81d8-0a2c-4fc6-96ac-59fe4cea3abf","8250d58d-f1a2-4397-bfd3-b2e688ba7141"],"externalId":"101010","orgLocation":[{"type":"state","id":"027f81d8-0a2c-4fc6-96ac-59fe4cea3abf"},{"type":"district","id":"8250d58d-f1a2-4397-bfd3-b2e688ba7141"}],"isRootOrg":true,"rootOrgId":"0126796199493140480","imgUrl":null,"homeUrl":null,"createdDate":"2019-01-18 09:48:13:428+0000","createdBy":"system","hashTagId":"0126796199493140480","status":1},"tcUpdatedDate":null,"userLocations":[{"code":"2814030006","name":"Zphs Gontuvanipalem","id":"1f339dfa-1c65-487d-9e15-27fbdbd337f5","type":"cluster","parentId":"e5be5e9c-3eea-4822-8754-9009c47c6782"},{"code":"2830","name":"Alluri Sita Rama Raju","id":"24c36610-0640-45a3-b88e-fa92c9ebbec2","type":"district","parentId":"bc75cc99-9205-463e-a722-5326857838f8"},{"code":"28","name":"Andhra Pradesh","id":"bc75cc99-9205-463e-a722-5326857838f8","type":"state","parentId":null},{"code":"281403","name":"Addateegala","id":"e5be5e9c-3eea-4822-8754-9009c47c6782","type":"block","parentId":"24c36610-0640-45a3-b88e-fa92c9ebbec2"},{"code":"28140306106","name":"APTWRS ADDATEEGALA","id":"01274200593719296013305","type":"school","parentId":""}],"userName":"nirmala_dohl","userId":"77f199bd-8ad2-4ecc-83b1-ea91bcc71ea0","declarations":[],"promptTnC":false,"lastLoginTime":0,"createdDate":"2024-05-24 05:48:14:548+0000","framework":{"board":["CBSE"],"gradeLevel":["Class 2"],"id":["ekstep_ncert_k-12"],"medium":["English"],"subject":["Business Studies"]},"createdBy":null,"profileUserType":{"subType":"hm","type":"administrator"},"tncAcceptedVersion":"v13"},"userRoleInformation":{"cluster":"1f339dfa-1c65-487d-9e15-27fbdbd337f5","district":"24c36610-0640-45a3-b88e-fa92c9ebbec2","state":"bc75cc99-9205-463e-a722-5326857838f8","block":"e5be5e9c-3eea-4822-8754-9009c47c6782","school":"28140306106","role":"HM"}}"""
}