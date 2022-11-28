package com.xktech.ixueto.model

data class Subject(
    val Id: Int,
    val Name: String,
    val CoverImageUrl: String,
    val CourseNumber: Int,
    val CourseHours: Int,
    val ExamineState: Short?
)
