package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.request.*
import com.xktech.ixueto.data.remote.entity.request.CourseStudy
import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.model.*
import retrofit2.http.*

interface StudyService {
    @GET("study/getPageStudySubject")
    suspend fun getPageStudySubject(@QueryMap options: Map<String, String>): Response<ResponsePage<StudySubject>>

    @GET("study/getPageStudyCourse")
    suspend fun getPageStudyCourse(
        @QueryMap options: Map<String, String>,
    ): Response<ResponsePage<StudyCourse>>

    @GET("study/getStudyCourseCount")
    suspend fun getStudyCourseCount(
        @Query("subjectId") subjectId: Int,
        @Query("type") type: Int,
    ): Response<Int>

    @GET("study/getStudySubjectDetails/{id}")
    suspend fun getStudySubjectDetails(
        @Path("id") id: Int,
    ): Response<StudySubjectDetails>

    @GET("study/getCourseInfo")
    suspend fun getCourseInfo(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
    ): Response<CourseInfo>

    @GET("study/rule")
    suspend fun getRule(): Response<Rule>

    @GET("study/needOnceFaceCheck")
    suspend fun needOnceFaceCheck(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
        @Query("faceCheckTimeRule") faceCheckTimeRule: Short,
    ): Response<Boolean>

    @GET("study/getSubjectStudyState")
    suspend fun getSubjectStudyState(): Response<SubjectStudyState>

    @GET("study/getCourseStudyState")
    suspend fun getCourseStudyState(@Query("subjectId") subjectId: Int): Response<CourseStudyState>

    @POST("study/faceCheck")
    suspend fun faceCheck(@Body faceCheckInfo: FaceCheckInfo): Response<FaceCheckResult>

    @POST("study/initCourse")
    suspend fun initCourse(@Body courseInit: CourseInit): Response<CourseInitResult>

    @POST("study/saveStudy")
    suspend fun saveStudy(@Body courseStudy: CourseStudy): Response<Boolean>

    @GET("study/getCourseStudyInfo/{id}")
    suspend fun getCourseStudyInfo(@Path("id") id: Int): Response<CourseStudyInfo>

    @POST("study/updateViolation")
    suspend fun updateViolation(@Body updateViolation: UpdateViolation): Response<Boolean>

    @POST("study/resetStudy")
    suspend fun resetStudy(@Body resetStudy: ResetStudy): Response<Boolean>

    @GET("study/getTimeStamp")
    suspend fun getTimeStamp(): Response<Long>

    @GET("study/getClassRule/{id}")
    suspend fun getClassRule(@Path("id") id: Int): Response<ClassRule>
}