package com.xktech.ixueto.model

import java.io.Serializable

data class StudyRule(
    val IsCourseRegularStudy: Boolean,
    val SubjectStudyRule: Short,
    val MinutesEveryCourseHours: Int,
    val MaxAllowStudyCourseHoursEveryDay: Int?
) : Serializable
