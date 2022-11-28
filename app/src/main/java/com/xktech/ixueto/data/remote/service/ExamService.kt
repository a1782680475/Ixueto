package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.request.ExamInit
import com.xktech.ixueto.data.remote.entity.request.RequestExamAllowEntry
import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.model.FormalExam
import com.xktech.ixueto.model.SimpleResult
import com.xktech.ixueto.model.TestExam
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ExamService {
    @POST("exam/initExam")
    suspend fun initExam(@Body examInit: ExamInit): Response<SimpleResult>
    @POST("exam/isAllowEntry")
    suspend fun isAllowEntry(@Body examAllowEntry: RequestExamAllowEntry): Response<SimpleResult>
    @GET("exam/getTestExams")
    suspend fun getTestExams(@Query("subjectId") subjectId: Int): Response<List<TestExam>>
    @GET("exam/getFormalExams")
    suspend fun getFormalExams(@Query("subjectId") subjectId: Int): Response<List<FormalExam>>
}