package com.xktech.ixueto.data.remote.entity.response

object ApiError {
    var unknownError = Error(20000, "未知错误")
    var netError = Error(20001, "网络错误")
    var emptyData = Error(20002, "没有数据")
}

data class Error(var errorCode: Int, var errorMsg: String)