package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.model.AuthenticationBrieInfo
import com.xktech.ixueto.model.AuthenticationInfo
import com.xktech.ixueto.model.AuthenticationItemConfig
import com.xktech.ixueto.model.Selector
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthenticationService {
    @GET("authentication/state")
    suspend fun getAuthenticationState(): Response<Short>

    @GET("authentication/Identity")
    suspend fun getIdentity(): Response<MutableList<Selector>>

    @GET("authentication/province")
    suspend fun getProvince(): Response<MutableList<Selector>>

    @GET("authentication/city")
    suspend fun getCity(@Query("provinceId") provinceId: String): Response<MutableList<Selector>>

    @GET("authentication/county")
    suspend fun getCounty(@Query("cityId") cityId: String): Response<MutableList<Selector>>

    @GET("authentication/street")
    suspend fun getStreet(@Query("countyId") countyId: String): Response<MutableList<Selector>>

    @GET("authentication/unit")
    suspend fun getUnit(@Query("cityId") cityId: String): Response<MutableList<Selector>>

    @GET("authentication/getAuthenticationConfig")
    suspend fun getAuthenticationConfig(@Query("identityId") identityId: Int): Response<Map<String, AuthenticationItemConfig>>

    @GET("authentication/getAuthenticationInfo")
    suspend fun getAuthenticationInfo(): Response<AuthenticationInfo>

    @GET("authentication/getAuthenticationBrieInfo")
    suspend fun getAuthenticationBrieInfo(): Response<AuthenticationBrieInfo>

    @POST("authentication/photoUpload")
    suspend fun photoUpload(@Body body: RequestBody): Response<String>

    @POST("authentication/saveAuthenticationInfo")
    suspend fun saveAuthenticationInfo(@Body authenticationInfo: AuthenticationInfo): Response<Boolean>
}