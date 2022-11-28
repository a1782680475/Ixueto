package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.xktech.ixueto.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ExamViewModel @Inject constructor(private val examRepository: ExamRepository) :
    ViewModel() {

    fun initExam(subjectId: Int, examType: Int) = liveData(Dispatchers.IO) {
        try {
            emit(examRepository.initExam(subjectId,examType))
        } catch (e: Exception) {

        }
    }

    fun isAllowEntry(subjectId: Int, examType: Int) = liveData(Dispatchers.IO) {
        try {
            emit(examRepository.isAllowEntry(subjectId, examType))
        } catch (e: Exception) {

        }
    }

    fun getTestExams(subjectId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(examRepository.getTestExams(subjectId))
        } catch (e: Exception) {

        }
    }

    fun getFormalExams(subjectId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(examRepository.getFormalExams(subjectId))
        } catch (e: Exception) {

        }
    }
}