package com.xktech.ixueto.model

data class ClassInfo(
    val ClassName: String,
    val WithinState: Int,
    val StartTime: String?,
    val EndTime: String?,
    val StartTimeEveryDay: String?,
    val EndTimeEveryDay: String?,
    val ExamStartTime: String?,
    val ExamEndTime: String?,
)