package com.xktech.ixueto.model

import java.io.Serializable

data class CourseStudyBack(
    var coursePosition: Int,
    var courseId: Int,
    var studyState: StudyStateEnum,
    var studyProgress: Double
) : Serializable