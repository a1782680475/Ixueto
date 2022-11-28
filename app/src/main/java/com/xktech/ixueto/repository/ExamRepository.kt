package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.ExamInit
import com.xktech.ixueto.data.remote.entity.request.RequestExamAllowEntry
import com.xktech.ixueto.data.remote.service.ExamService
import com.xktech.ixueto.model.FormalExam
import com.xktech.ixueto.model.SimpleResult
import com.xktech.ixueto.model.TestExam
import javax.inject.Inject

class ExamRepository @Inject constructor(private var examService: ExamService) {
    suspend fun initExam(subjectId: Int, examType: Int): SimpleResult {
        return examService
            .initExam(ExamInit(subjectId, examType)).data!!
    }

    suspend fun isAllowEntry(subjectId: Int, examType: Int): SimpleResult {
        return examService
            .isAllowEntry(RequestExamAllowEntry(subjectId, examType)).data!!
    }

    suspend fun getTestExams(subjectId: Int): List<TestExam> {
        return examService
            .getTestExams(subjectId).data!!
    }

    suspend fun getFormalExams(subjectId: Int): List<FormalExam> {
        return examService
            .getFormalExams(subjectId).data!!
    }
}