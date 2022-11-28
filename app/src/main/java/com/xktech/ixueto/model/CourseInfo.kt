package com.xktech.ixueto.model
import java.io.*

data class CourseInfo(
    var StuSubId: Int,
    val SubjectId: Int,
    var ProfessionId: Int,
    var GradeId: Int,
    val CourseId: Int,
    val CourseName: String,
    val VideoUrl: String,
    val VideoSeconds: Long
): Serializable