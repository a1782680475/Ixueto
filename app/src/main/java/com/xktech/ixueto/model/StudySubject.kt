package com.xktech.ixueto.model

data class StudySubject(
    val Id: Int,
    val ProfessionId: Int,
    val GradeId: Int,
    val ExamineState: Short,
    val ProfessionName: String,
    val SubjectName: String,
    val GradeName: String,
    val CoverImageUrl: String,
    val UnitName: String,
    val ChapterNumber: Int,
    val CourseHours: Int,
    val StudyState: Short,
    val Progress: Double,
    val ClassId: Int?,
    val WithinState: Short,
    val StartTime: String?,
    val EndTime: String?,
    val StartTimeEveryDay: String?,
    val EndTimeEveryDay: String?
)
