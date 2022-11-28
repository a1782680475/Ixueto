package com.xktech.ixueto.data.remote.entity.request

data class FaceCheckInfo(
    val StudyId: Int,
    val StuSubId: Int,
    val SubjectId: Int,
    val ProfessionId: Int,
    val CourseId: Int,
    val GradeId: Int,
    val Stage: Int,
    val FaceDataBase64: String,
)