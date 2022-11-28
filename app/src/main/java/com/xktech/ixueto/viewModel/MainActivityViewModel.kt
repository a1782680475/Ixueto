package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import com.xktech.ixueto.di.NetworkModule
import com.xktech.ixueto.repository.UserInfoPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val userInfoPreferencesRepository: UserInfoPreferencesRepository,
) : ViewModel() {
    var isFirstLoad = true
    var isVersionChecked = false
    fun trySetToken(): String? {
        if (NetworkModule.token.isNullOrEmpty()) {
            NetworkModule.token = userInfoPreferencesRepository.getUserInfoSync().token
        }
        return NetworkModule.token
    }

}