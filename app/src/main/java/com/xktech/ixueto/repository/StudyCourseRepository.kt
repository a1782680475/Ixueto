package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.RequestStudyCourse
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.data.remote.service.StudyService
import com.xktech.ixueto.model.StudyCourse
import javax.inject.Inject

class StudyCourseRepository @Inject constructor(private var studyService: StudyService) {
    suspend fun getData(request: RequestStudyCourse): ResponsePage<StudyCourse> {
        var map = mapOf(
            "subjectId" to request.subjectId.toString(),
            "type" to request.type.toString(),
            "page" to request.page.toString(),
            "pageSize" to request.pageSize.toString(),
        )
        return studyService
            .getPageStudyCourse(map).data!!
    }
}