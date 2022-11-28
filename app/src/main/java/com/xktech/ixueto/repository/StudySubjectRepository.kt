package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.RequestStudySubject
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.data.remote.service.StudyService
import com.xktech.ixueto.model.StudySubject
import com.xktech.ixueto.model.StudySubjectDetails
import javax.inject.Inject

class StudySubjectRepository @Inject constructor(private val studyService: StudyService) {
    suspend fun getData(request: RequestStudySubject): ResponsePage<StudySubject> {
        var map = mapOf(
            "type" to request.type.toString(),
            "page" to request.page.toString(),
            "pageSize" to request.pageSize.toString(),
        )
        return studyService
            .getPageStudySubject(map).data!!
    }
    suspend fun getStudySubjectDetails(id: Int): StudySubjectDetails {
        return studyService
            .getStudySubjectDetails(id).data!!
    }
}