package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.*
import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.data.remote.service.UserService
import com.xktech.ixueto.model.*
import javax.inject.Inject

class UserRepository @Inject constructor(var userService: UserService) {
    suspend fun login(username: String, password: String): Response<LoginResult> {
        //val passwordEncrypt = Encrypt.customEncrypt(password)
        return userService.login(RequestLogin(username, password))
    }

    suspend fun getUserInfo(): Response<UserInfo> {
        return userService.getUserInfo()
    }

    suspend fun getStudyProgress(): StudyProgress {
        return userService.getStudyProgress().data!!
    }

    suspend fun modifyPassword(modifyPassword: ModifyPassword): SimpleResult {
        return userService.modifyPassword(modifyPassword).data!!
    }

    suspend fun sendCodeForResetPassword(phone: String): SimpleResult {
        return userService.sendCodeForResetPassword(RequestSendCode(phone)).data!!
    }

    suspend fun checkPhoneCode(phone: String, code: String): CheckPhoneCodeResult {
        return userService.checkPhoneCode(RequestCheckPhoneCode(phone, code)).data!!
    }

    suspend fun resetPassword(token: String, password: String): SimpleResult {
        return userService.resetPassword(RequestResetPassword(token, password)).data!!
    }

    suspend fun sendCodeForRegister(phone: String): SimpleResult {
        return userService.sendCodeForRegister(RequestSendCode(phone)).data!!
    }

    suspend fun register(phone: String, password: String, code: String): SimpleResult {
        return userService.register(RequestRegister(phone, password, code)).data!!
    }
}