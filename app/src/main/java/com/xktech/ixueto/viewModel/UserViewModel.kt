package com.xktech.ixueto.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.xktech.ixueto.datastore.UserInfo
import com.xktech.ixueto.di.NetworkModule
import com.xktech.ixueto.model.ModifyPassword
import com.xktech.ixueto.model.StudyProgress
import com.xktech.ixueto.repository.UserInfoPreferencesRepository
import com.xktech.ixueto.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
open class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userInfoPreferencesRepository: UserInfoPreferencesRepository,
) : ViewModel() {
    private var _userInfo: UserInfo? = null
    private var _studyProgress = MutableLiveData<StudyProgress?>(null)

    fun getUserInfo() = liveData<UserInfo> {
        try {
            if (_userInfo != null && !_userInfo!!.token.isNullOrEmpty()) {
                emit(_userInfo!!)
            } else {
                _userInfo = userInfoPreferencesRepository.getUserInfo()
                emit(_userInfo!!)
            }
        } catch (e: Exception) {

        }
    }

    val studyProgress = _studyProgress

    fun getStudyProgressByRemote() {
        try {
            viewModelScope.launch(Dispatchers.Main) {
                _studyProgress.value = withContext(Dispatchers.IO) {
                    userRepository.getStudyProgress()
                }
            }
        } catch (e: Exception) {

        }
    }

    fun login(username: String, password: String): MutableLiveData<Boolean> {
        return MutableLiveData<Boolean>().also {
            try {
                viewModelScope.launch(Dispatchers.Main) {
                    it.value = withContext(Dispatchers.IO) {
                        var result = userRepository.login(username, password)
                        var userInfo: com.xktech.ixueto.model.UserInfo =
                            com.xktech.ixueto.model.UserInfo()
                        if (result.code == 200) {
                            NetworkModule.token = result.data!!.AppToken
                            userInfo.Token = result.data!!.AppToken
                            userInfo.WebToken = result.data!!.WebToken
                            userInfo.DesUsername =
                                result.data!!.DESUserInfo?.get("username").toString()
                            userInfo.DesUserType =
                                result.data!!.DESUserInfo?.get("usertype").toString()
                            userInfoPreferencesRepository.setUserInfoSync(userInfo)
                            val userInfoFromWeb = userRepository.getUserInfo().data
                            userInfo.UserId = userInfoFromWeb?.UserId!!
                            userInfo.UserName = userInfoFromWeb?.UserName.toString()
                            userInfo.Name = userInfoFromWeb?.Name.toString()
                            userInfoFromWeb?.Phone?.let {
                                userInfo.Phone = userInfoFromWeb?.Phone!!
                            }
                            userInfoFromWeb?.Avatar?.let {
                                userInfo.Avatar = userInfoFromWeb?.Avatar!!
                            }
                            userInfoPreferencesRepository.setUserInfoSync(userInfo)
                            true
                        } else {
                            false
                        }
                    }
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    fun modifyPassword(originalPassword: String, newPassword: String) = liveData {
        try {
            emit(userRepository.modifyPassword(ModifyPassword(originalPassword, newPassword)))
        } catch (e: Exception) {

        }
    }

    fun sendCodeForResetPassword(phone: String) = liveData {
        try {
            emit(userRepository.sendCodeForResetPassword(phone))
        } catch (e: Exception) {

        }
    }

    fun checkPhoneCode(phone: String, code: String) = liveData {
        try {
            emit(userRepository.checkPhoneCode(phone, code))
        } catch (e: Exception) {

        }
    }

    fun resetPassword(token: String, password: String) = liveData {
        try {
            emit(userRepository.resetPassword(token, password))
        } catch (e: Exception) {

        }
    }

    fun sendCodeForRegister(phone: String) = liveData {
        try {
            emit(userRepository.sendCodeForRegister(phone))
        } catch (e: Exception) {

        }
    }

    fun register(phone: String, password: String, code: String) = liveData {
        try {
            emit(userRepository.register(phone, password, code))
        } catch (e: Exception) {

        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userInfoPreferencesRepository.setUserInfoSync(null)
                _userInfo = null
                _studyProgress.value = null
                NetworkModule.token = null
            } catch (e: Exception) {

            }
        }
    }
}