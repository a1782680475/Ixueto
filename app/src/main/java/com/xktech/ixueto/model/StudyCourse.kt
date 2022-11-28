package com.xktech.ixueto.model

data class StudyCourse(
    val Id: Int,
    val Rank: Int,
    var Name: String,
    var TimeLength: String,
    var StudyTimeLength: String,
    var StudyState: Short,
    var Progress: Double
)
