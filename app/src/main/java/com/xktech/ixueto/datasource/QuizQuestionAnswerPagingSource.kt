package com.xktech.ixueto.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xktech.ixueto.data.remote.entity.request.RequestQuizQuestionAnswerBrief
import com.xktech.ixueto.model.QuizQuestionAnswerBrief
import com.xktech.ixueto.repository.QuizRepository
import kotlin.math.ceil

class QuizQuestionAnswerPagingSource  (
    private val quizRepository: QuizRepository,
    private val request: RequestQuizQuestionAnswerBrief
) : PagingSource<Int, QuizQuestionAnswerBrief>() {
    override fun getRefreshKey(state: PagingState<Int, QuizQuestionAnswerBrief>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuizQuestionAnswerBrief> {
        try {
            var currentPage = params.key ?: 1
            request.page = currentPage
            val response = quizRepository.getPageQuestionAnswerBrief(request)
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