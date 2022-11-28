package com.xktech.ixueto.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xktech.ixueto.data.remote.entity.request.RequestStudySubject
import com.xktech.ixueto.model.StudySubject
import com.xktech.ixueto.repository.StudySubjectRepository
import kotlin.math.ceil

class StudySubjectPagingSource(
    private val studySubjectRepository: StudySubjectRepository,
    private val request: RequestStudySubject
) : PagingSource<Int, StudySubject>() {
    override fun getRefreshKey(state: PagingState<Int, StudySubject>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StudySubject> {
        try {
            var currentPage = params.key ?: 1
            request.page = currentPage
            val response = studySubjectRepository.getData(request)
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