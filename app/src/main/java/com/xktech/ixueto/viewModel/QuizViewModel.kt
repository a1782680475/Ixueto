package com.xktech.ixueto.viewModel

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.xktech.ixueto.data.remote.entity.request.QuizAnswer
import com.xktech.ixueto.data.remote.entity.request.RequestQuizQuestionAnswerBrief
import com.xktech.ixueto.datasource.QuizQuestionAnswerPagingSource
import com.xktech.ixueto.repository.QuizRepository
import com.xktech.ixueto.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val studyRepository: StudyRepository,
) : ViewModel() {
    val request = RequestQuizQuestionAnswerBrief()

    fun initQuiz(professionId: Int, subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(quizRepository.initQuiz(professionId, subjectId, courseId))
        } catch (e: Exception) {

        }
    }

    fun getQuestions(subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(quizRepository.getQuestions(subjectId, courseId))
        } catch (e: Exception) {

        }
    }

    fun getQuestionAnswer(subjectId: Int, courseId: Int, questionId: Int) =
        liveData(Dispatchers.IO) {
            try {
                emit(quizRepository.getQuestionAnswer(subjectId, courseId, questionId))
            } catch (e: Exception) {

            }
        }

    fun getPageQuestionAnswerBrief(subjectId: Int, courseId: Int) = Pager(
        PagingConfig(pageSize = request.pageSize)
    ) {
        request.subjectId = subjectId
        request.courseId = courseId
        QuizQuestionAnswerPagingSource(quizRepository, request)
    }.flow

    fun getQuizResult(subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(quizRepository.getQuizResult(subjectId, courseId))
        } catch (e: Exception) {

        }
    }

    fun getQuestion(subjectId: Int, courseId: Int, questionId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(quizRepository.getQuestion(subjectId, courseId, questionId))
        } catch (e: Exception) {

        }
    }

    fun getQuizRuleAndQuizNumber(subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            val rule = studyRepository.getRule()
            val quizNumber = quizRepository.getQuizNumber(subjectId, courseId)
            emit(Pair(rule, quizNumber))
        } catch (e: Exception) {

        }
    }

    fun saveAnswer(
        professionId: Int,
        subjectId: Int,
        courseId: Int,
        questionId: Int,
        answers: MutableList<String>,
    ) = liveData(Dispatchers.IO) {
        try {
            emit(
                quizRepository.saveAnswer(
                    QuizAnswer(
                        professionId,
                        subjectId,
                        courseId,
                        questionId,
                        answers
                    )
                )
            )
        } catch (e: Exception) {

        }
    }

    fun submit(
        professionId: Int,
        subjectId: Int,
        courseId: Int,
        questionId: Int?,
        answers: MutableList<String>,
    ) = liveData(Dispatchers.IO) {
        try {
            emit(
                quizRepository.submit(
                    QuizAnswer(
                        professionId,
                        subjectId,
                        courseId,
                        questionId,
                        answers
                    )
                )
            )
        } catch (e: Exception) {

        }
    }

    fun getAnswerSheet(
        subjectId: Int,
        courseId: Int,
    ) = liveData(Dispatchers.IO) {
        try {
            emit(quizRepository.getAnswerSheet(subjectId, courseId))
        } catch (e: Exception) {

        }
    }
}