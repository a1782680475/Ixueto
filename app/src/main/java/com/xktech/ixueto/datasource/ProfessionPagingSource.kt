package com.xktech.ixueto.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xktech.ixueto.data.remote.entity.request.RequestProfession
import com.xktech.ixueto.model.Profession
import com.xktech.ixueto.repository.ProfessionRepository
import kotlin.math.ceil

class ProfessionPagingSource (
    private val professionRepository: ProfessionRepository,
    private val request: RequestProfession
) : PagingSource<Int, Profession>() {
    override fun getRefreshKey(state: PagingState<Int, Profession>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Profession> {
        try {
            var currentPage = params.key ?: 1
            request.page = currentPage
            val response = professionRepository.getData(request)
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