package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.xktech.ixueto.repository.NoticePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel
@Inject
constructor(private val noticePreferencesRepository: NoticePreferencesRepository) : ViewModel() {
    fun getNotice() = liveData {
        emit(noticePreferencesRepository.getNotice())
    }

    fun read() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                noticePreferencesRepository.read()
            }
        }
    }
}