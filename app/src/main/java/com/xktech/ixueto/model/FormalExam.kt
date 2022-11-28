package com.xktech.ixueto.model

data class FormalExam(
    var SubjectId: Int,
    var ProfessionId: Int,
    var GradeId: Int,
    val ExamId: Int,
    val AdmissionNumber: String,
    val ExamName: String,
    val ExamTimeMinutes: Int?,
    val FullScore: Double,
    val PassScore: Double,
    val AllowSeeScore: Boolean,
    val Score: Double?,
    val AccessToken: String
)