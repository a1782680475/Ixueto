package com.xktech.ixueto.model

import java.io.Serializable

data class CourseStudyInfo(
    var StudiedSeconds: Long,
    var IsFinished: Boolean,
    var Progress: Double,
    var LastFaceCheckFinished: Boolean,
    var TotalStudiedSecondsToday: Long,
    var StudiedSecondsToday: Long,
    var ViolateNumber: Int,
    var QuizState: Short,
    var IsExamAllowed: Boolean,
) : Serializable
