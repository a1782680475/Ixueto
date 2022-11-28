package com.xktech.ixueto.data.remote.entity.request

data class CourseInit(
    val subjectId: Int,
    val courseId: Int,
    val studiedSeconds: Long?,
    val studiedSecondsToday: Long?,
    val lastStudyTimeStamp: Long?
)
