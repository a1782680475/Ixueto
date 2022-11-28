package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.xktech.ixueto.datastore.Setting
import com.xktech.ixueto.repository.SettingPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingPreferencesRepository: SettingPreferencesRepository,
) : ViewModel() {
    fun getSetting() = liveData {
        emit(settingPreferencesRepository.getSetting())
    }

    fun setAlertAtNotWifi(enable: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                settingPreferencesRepository.setAlertAtNotWifi(enable)
            }
        }
    }

    fun setGesture(enable: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                settingPreferencesRepository.setGesture(enable)
            }
        }
    }

    fun setStartupPage(index: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                settingPreferencesRepository.setStartupPage(index)
            }
        }
    }

    fun setFacePageBright(enable: Boolean){
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                settingPreferencesRepository.setFacePageBright(enable)
            }
        }
    }

    fun getSettingSync(): Setting {
        return settingPreferencesRepository.getSettingSync()
    }
}