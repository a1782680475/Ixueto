package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.xktech.ixueto.data.remote.entity.request.RequestCourse
import com.xktech.ixueto.datasource.CoursePagingSource
import com.xktech.ixueto.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(private val courseRepository: CourseRepository) :
    ViewModel() {
    private val request = RequestCourse()
    fun getCourses(subjectId: Int) = Pager(
        PagingConfig(pageSize = request.pageSize)
    ) {
        request.subjectId = subjectId
        CoursePagingSource(courseRepository, request)
    }.flow
}