package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.xktech.ixueto.data.remote.entity.request.RequestStudyCourse
import com.xktech.ixueto.datasource.StudyCoursePagingSource
import com.xktech.ixueto.repository.StudyCourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StudyCourseViewModel @Inject constructor(private val studyCourseRepository: StudyCourseRepository) :
    ViewModel() {
    private val request = RequestStudyCourse()
    fun getStudyCourses(subjectId: Int, type: Int) = Pager(
        PagingConfig(pageSize = request.pageSize)
    ) {
        request.subjectId = subjectId
        request.type = type
        StudyCoursePagingSource(studyCourseRepository, request)
    }.flow

}