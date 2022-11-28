package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.xktech.ixueto.data.remote.entity.request.RequestProfession
import com.xktech.ixueto.datasource.ProfessionPagingSource
import com.xktech.ixueto.repository.ProfessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ProfessionViewModel @Inject constructor(private val professionRepository: ProfessionRepository) :
    ViewModel() {
    private val request = RequestProfession()
    fun getProfessions(regionId: Int) = Pager(
        PagingConfig(pageSize = request.pageSize)
    ) {
        request.regionId = regionId
        ProfessionPagingSource(professionRepository, request)
    }.flow

    fun searchProfession(
        regionId: Int,
        professionName: String,
    ) = liveData (Dispatchers.IO) {
        request.regionId = regionId
        request.professionName = professionName
        request.pageSize = 10
        emit(
            try {
                professionRepository.getData(request).Rows
            } catch (e: Exception) {
                mutableListOf()
            }
        )
    }
}
