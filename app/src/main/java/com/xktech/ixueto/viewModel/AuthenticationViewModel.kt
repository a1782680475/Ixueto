package com.xktech.ixueto.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.xktech.ixueto.model.AuthenticationBasis
import com.xktech.ixueto.model.AuthenticationInfo
import com.xktech.ixueto.model.PhotoTypeEnum
import com.xktech.ixueto.repository.AuthenticationRepository
import com.xktech.ixueto.utils.EnumUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepository) :
    ViewModel() {
    val authenticationState = liveData {
        try {
            emit(EnumUtils.getAuthenticationState(authenticationRepository.getAuthenticationState()))
        } catch (e: Exception) {

        }
    }

    val identity = liveData {
        try {
            emit(authenticationRepository.getIdentity())
        } catch (e: Exception) {

        }

    }

    val province = liveData {
        try {
            emit(authenticationRepository.getProvince())
        } catch (e: Exception) {

        }
    }

    fun city(provinceId: String) = liveData {
        try {
            emit(authenticationRepository.getCity(provinceId))
        } catch (e: Exception) {

        }
    }

    fun county(cityId: String) = liveData {
        try {
            emit(authenticationRepository.getCounty(cityId))
        } catch (e: Exception) {

        }
    }

    fun street(countyId: String) = liveData {
        try {
            emit(authenticationRepository.getStreet(countyId))
        } catch (e: Exception) {

        }
    }

    fun unit(cityId: String) = liveData {
        try {
            emit(authenticationRepository.getUnit(cityId))
        } catch (e: Exception) {

        }
    }

    fun getAuthenticationConfig(identityId: Int) = liveData {
        try {
            emit(authenticationRepository.getAuthenticationConfig(identityId))
        } catch (e: Exception) {

        }
    }

    fun getAuthenticationBasis() = liveData {
        try {
            val authenticationState = viewModelScope.async {
                authenticationRepository.getAuthenticationState()
            }
            val authenticationInfo = viewModelScope.async {
                authenticationRepository.getAuthenticationInfo()
            }
            emit(
                AuthenticationBasis(
                    EnumUtils.getAuthenticationState(authenticationState.await()),
                    authenticationInfo.await()
                )
            )
        } catch (e: Exception) {

        }
    }

    fun getAuthenticationBrieInfo() = liveData {
        try {
            emit(authenticationRepository.getAuthenticationBrieInfo())
        } catch (e: Exception) {

        }
    }

    fun photoUpload(photoType: PhotoTypeEnum, file: File) = liveData {
        try {
            emit(authenticationRepository.photoUpload(photoType, file))
        } catch (e: Exception) {

        }
    }

    fun photoUpload(photoType: PhotoTypeEnum, bitmap: Bitmap) = liveData {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            emit(authenticationRepository.photoUpload(photoType, bytes))
        } catch (e: Exception) {

        }
    }

    fun saveAuthenticationInfo(authenticationInfo: AuthenticationInfo) = liveData {
        try {
            emit(authenticationRepository.saveAuthenticationInfo(authenticationInfo))
        } catch (e: Exception) {

        }
    }
}