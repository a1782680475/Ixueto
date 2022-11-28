package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.xktech.ixueto.data.remote.entity.request.RequestStudySubject
import com.xktech.ixueto.datasource.StudySubjectPagingSource
import com.xktech.ixueto.repository.StudySubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class StudySubjectViewModel @Inject constructor(private val studySubjectRepository: StudySubjectRepository) :
    ViewModel() {
    private val request = RequestStudySubject()
    private fun getStudySubjects(type: Short) = Pager(
        PagingConfig(pageSize = request.pageSize)
    ) {
        request.type = type
        StudySubjectPagingSource(studySubjectRepository, request)
    }.flow

    fun getPersonalStudySubjects() = getStudySubjects(0)
    fun getClassesStudySubjects() = getStudySubjects(1)
    fun getStudySubjectDetails(subjectId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(studySubjectRepository.getStudySubjectDetails(subjectId))
        } catch (e: Exception) {

        }
    }
}