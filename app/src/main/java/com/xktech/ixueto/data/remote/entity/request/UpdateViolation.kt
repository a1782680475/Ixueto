package com.xktech.ixueto.data.remote.entity.request

data class UpdateViolation(
    var StudyId: Int,
    var SubjectId: Int,
    var CourseId: Int,
    var ViolateCount: Int,
)