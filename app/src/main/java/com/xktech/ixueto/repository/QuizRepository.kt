package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.QuizAnswer
import com.xktech.ixueto.data.remote.entity.request.RequestQuizQuestionAnswerBrief
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.data.remote.service.QuizService
import com.xktech.ixueto.model.*
import javax.inject.Inject

class QuizRepository @Inject constructor(private var quizService: QuizService) {

    suspend fun initQuiz(professionId: Int, subjectId: Int, courseId: Int): QuizInit {
        return quizService.initQuiz(professionId, subjectId, courseId).data!!
    }

    suspend fun getQuestions(subjectId: Int, courseId: Int): MutableList<Int> {
        return quizService.getQuestions(subjectId, courseId).data!!
    }

    suspend fun getQuestionAnswer(
        subjectId: Int,
        courseId: Int,
        questionId: Int,
    ): QuizQuestionAnswer {
        return quizService.getQuestionAnswer(subjectId, courseId, questionId).data!!
    }

    suspend fun getPageQuestionAnswerBrief(
        requestQuizQuestionAnswerBrief: RequestQuizQuestionAnswerBrief,
    ): ResponsePage<QuizQuestionAnswerBrief> {
        return quizService.getPageQuestionAnswerBrief(requestQuizQuestionAnswerBrief.subjectId,
            requestQuizQuestionAnswerBrief.courseId,
            requestQuizQuestionAnswerBrief.page,
            requestQuizQuestionAnswerBrief.pageSize).data!!
    }

    suspend fun getQuizResult(
        subjectId: Int,
        courseId: Int,
    ): QuizResult {
        return quizService.getQuizResult(subjectId, courseId).data!!
    }

    suspend fun getQuestion(
        subjectId: Int,
        courseId: Int,
        questionId: Int,
    ): QuizQuestion {
        return quizService.getQuestion(subjectId, courseId, questionId).data!!
    }

    suspend fun getQuizNumber(
        subjectId: Int,
        courseId: Int,
    ): Int {
        return quizService.getQuizNumber(subjectId, courseId).data!!
    }

    suspend fun saveAnswer(
        quizAnswer: QuizAnswer,
    ): Boolean{
        return quizService.saveAnswer(quizAnswer).data!!
    }

    suspend fun submit(
        quizAnswer: QuizAnswer,
    ): Short{
        return quizService.submit(quizAnswer).data!!
    }

    suspend fun getAnswerSheet(
        subjectId: Int,
        courseId: Int,
    ): MutableList<AnswerSheetItem>{
        return quizService.getAnswerSheet(subjectId,courseId).data!!
    }
}