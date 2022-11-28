package com.xktech.ixueto.model

import com.xktech.ixueto.data.local.entity.CourseStudyData
import com.xktech.ixueto.data.local.entity.CourseStudyDayData

data class LocalStudyData(
    var courseStudyData: CourseStudyData?,
    var courseStudyDayData: CourseStudyDayData?
)
