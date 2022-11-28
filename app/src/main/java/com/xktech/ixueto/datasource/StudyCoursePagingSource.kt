package com.xktech.ixueto.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xktech.ixueto.data.remote.entity.request.RequestStudyCourse
import com.xktech.ixueto.model.StudyCourse
import com.xktech.ixueto.repository.StudyCourseRepository
import kotlin.math.ceil

class StudyCoursePagingSource (
    private val studyCourseRepository: StudyCourseRepository,
    private val request: RequestStudyCourse
) : PagingSource<Int, StudyCourse>() {
    override fun getRefreshKey(state: PagingState<Int, StudyCourse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StudyCourse> {
        try {
            var currentPage = params.key ?: 1
            request.page = currentPage
            val response = studyCourseRepository.getData(request)
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