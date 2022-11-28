package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.xktech.ixueto.repository.VersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class VersionViewModel @Inject constructor(versionRepository: VersionRepository) : ViewModel() {
    val version = liveData (Dispatchers.IO) {
        try {
            emit(versionRepository.versionCheck())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}