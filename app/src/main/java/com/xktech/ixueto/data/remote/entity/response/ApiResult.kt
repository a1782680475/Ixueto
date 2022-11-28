package com.xktech.ixueto.data.remote.entity.response

sealed class ApiResult<out T>() {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val errorCode: Int, val errorMsg: String) : ApiResult<Nothing>()
}