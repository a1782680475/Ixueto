package com.xktech.ixueto.data.remote.entity.request

data class CourseStudy(
    val subjectId: Int,
    val professionId: Int,
    val gradeId: Int,
    val courseId: Int,
    val studiedSeconds: Long?,
    val studiedSecondsToday: Long?,
    val studyDetailId: Int,
)
