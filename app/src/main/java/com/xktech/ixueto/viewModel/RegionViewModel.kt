package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.xktech.ixueto.repository.RegionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RegionViewModel @Inject constructor(private val regionRepository: RegionRepository) :
    ViewModel() {
    val regions = liveData (Dispatchers.IO) {
        try {
            emit(regionRepository.getRegions())
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}