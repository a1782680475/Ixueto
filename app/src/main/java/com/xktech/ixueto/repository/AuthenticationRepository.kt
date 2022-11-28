package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.data.remote.service.AuthenticationService
import com.xktech.ixueto.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(private val authenticationService: AuthenticationService) {
    suspend fun getAuthenticationState(): Short {
        return authenticationService.getAuthenticationState().data!!
    }

    suspend fun getIdentity(): MutableList<Selector> {
        return authenticationService.getIdentity().data!!
    }

    suspend fun getProvince(): MutableList<Selector> {
        return authenticationService.getProvince().data!!
    }

    suspend fun getCity(provinceId: String): MutableList<Selector> {
        return authenticationService.getCity(provinceId).data!!
    }

    suspend fun getCounty(cityId: String): MutableList<Selector> {
        return authenticationService.getCounty(cityId).data!!
    }

    suspend fun getStreet(countyId: String): MutableList<Selector> {
        return authenticationService.getStreet(countyId).data!!
    }

    suspend fun getUnit(cityId: String): MutableList<Selector> {
        return authenticationService.getUnit(cityId).data!!
    }

    suspend fun getAuthenticationConfig(identityId: Int): Map<String, AuthenticationItemConfig> {
        return authenticationService.getAuthenticationConfig(identityId).data!!
    }

    suspend fun getAuthenticationInfo(): AuthenticationInfo {
        return authenticationService.getAuthenticationInfo().data!!
    }

    suspend fun getAuthenticationBrieInfo(): AuthenticationBrieInfo {
        return authenticationService.getAuthenticationBrieInfo().data!!
    }

    suspend fun photoUpload(photoType: PhotoTypeEnum, file: File): String {
        val body: RequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        var part = MultipartBody.Part.createFormData("file", file.name, body)
        val params = mutableMapOf("photoType" to photoType.value.toString())
        val mulBody = MultipartBody.Builder().apply {
            addPart(part)
            params.map {
                addFormDataPart(it.key, it.value)
            }
            setType("multipart/form-data".toMediaTypeOrNull()!!)
        }.build()
        return authenticationService.photoUpload(mulBody).data!!
    }

    suspend fun photoUpload(photoType: PhotoTypeEnum, bitArray: ByteArray): String {
        val body: RequestBody =  bitArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        var part = MultipartBody.Part.createFormData("file", "file_${photoType.name.lowercase(Locale.getDefault())}.jpeg", body)
        val params = mutableMapOf("photoType" to photoType.value.toString())
        val mulBody = MultipartBody.Builder().apply {
            addPart(part)
            params.map {
                addFormDataPart(it.key, it.value)
            }
            setType("multipart/form-data".toMediaTypeOrNull()!!)
        }.build()
        return authenticationService.photoUpload(mulBody).data!!
    }

    suspend fun saveAuthenticationInfo(authenticationInfo: AuthenticationInfo): Response<Boolean> {
        return authenticationService.saveAuthenticationInfo(authenticationInfo)
    }
}