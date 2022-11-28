package com.xktech.ixueto.model

import java.io.Serializable

data class CourseStudy(
    var courseInfo: CourseInfo,
    var courseStudyInfo: CourseStudyInfo
): Serializable
