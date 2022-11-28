package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.request.*
import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
    @POST("user/login")
    suspend fun login(@Body requestLogin: RequestLogin): Response<LoginResult>

    @GET("user/getAccountInfo")
    suspend fun getUserInfo(): Response<UserInfo>

    @GET("user/getStudyProgress")
    suspend fun getStudyProgress(): Response<StudyProgress>

    @POST("user/passwordModify")
    suspend fun modifyPassword(@Body modifyPassword: ModifyPassword): Response<SimpleResult>

    @POST("user/sendCodeForResetPassword")
    suspend fun sendCodeForResetPassword(@Body requestSendCode: RequestSendCode): Response<SimpleResult>

    @POST("user/checkPhoneCode")
    suspend fun checkPhoneCode(@Body requestCheckPhoneCode: RequestCheckPhoneCode): Response<CheckPhoneCodeResult>

    @POST("user/resetPassword")
    suspend fun resetPassword(@Body requestResetPassword: RequestResetPassword): Response<SimpleResult>

    @POST("user/sendCodeForRegister")
    suspend fun sendCodeForRegister(@Body requestSendCode: RequestSendCode): Response<SimpleResult>

    @POST("user/register")
    suspend fun register(@Body requestRegister: RequestRegister): Response<SimpleResult>
}