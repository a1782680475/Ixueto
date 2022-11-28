package com.xktech.ixueto.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xktech.ixueto.data.remote.entity.request.RequestCourse
import com.xktech.ixueto.data.remote.entity.request.RequestStudyCourse
import com.xktech.ixueto.model.Course
import com.xktech.ixueto.repository.CourseRepository
import kotlin.math.ceil

class CoursePagingSource (
    private val courseRepository: CourseRepository,
    private val request: RequestCourse
) : PagingSource<Int, Course>() {
    override fun getRefreshKey(state: PagingState<Int, Course>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Course> {
        try {
            var currentPage = params.key ?: 1
            request.page = currentPage
            val response = courseRepository.getData(request)
            val total = response.Total
            var pageCount = ceil(total.toDouble() / request.pageSize)
            val nextPage = if (currentPage < pageCount) {
                currentPage + 1
            } else {
                null
            }
            return LoadResult.Page(
                data = response.Rows,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            return LoadResult.Error(throwable = e)
        }
    }
}