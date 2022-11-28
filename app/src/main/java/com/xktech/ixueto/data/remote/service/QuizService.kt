package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.request.QuizAnswer
import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface QuizService {
    @GET("quiz/initQuiz")
    suspend fun initQuiz(
        @Query("professionId") professionId: Int,
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
    ): Response<QuizInit>

    @GET("quiz/getQuestions")
    suspend fun getQuestions(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
    ): Response<MutableList<Int>>

    @GET("quiz/getQuestionAnswer")
    suspend fun getQuestionAnswer(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
        @Query("questionId") questionId: Int,
    ): Response<QuizQuestionAnswer>

    @GET("quiz/getQuestion")
    suspend fun getQuestion(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
        @Query("questionId") questionId: Int,
    ): Response<QuizQuestion>

    @GET("quiz/getPageQuestionAnswerBrief")
    suspend fun getPageQuestionAnswerBrief(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): Response<ResponsePage<QuizQuestionAnswerBrief>>

    @GET("quiz/getQuizResult")
    suspend fun getQuizResult(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
    ): Response<QuizResult>

    @GET("quiz/getQuizNumber")
    suspend fun getQuizNumber(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
    ): Response<Int>

    @POST("quiz/saveAnswer")
    suspend fun saveAnswer(
        @Body quizAnswer: QuizAnswer,
    ): Response<Boolean>

    @POST("quiz/submit")
    suspend fun submit(
        @Body quizAnswer: QuizAnswer,
    ): Response<Short>

    @GET("quiz/getAnswerSheet")
    suspend fun getAnswerSheet(
        @Query("subjectId") subjectId: Int,
        @Query("courseId") courseId: Int,
    ):Response<MutableList<AnswerSheetItem>>
}