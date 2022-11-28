package com.xktech.ixueto.model

data class StudySubjectDetails(
    val SubjectId: Int,
    val ProfessionId: Int,
    val GradeId: Int,
    val SubjectName: String,
    val ProfessionName:String,
    val UnitName:String,
    val ChapterNumber: Int,
    val CourseHours: Int,
    val FinishChapterNumber: Int?,
    val FinishCourseHours: Int?,
    val StudyProgress: Double?,
    val ClassInfo: ClassInfo?
)