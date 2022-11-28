package com.xktech.ixueto.data.remote.entity.response

class ApiException : Exception {
    var errorCode: Int
        private set
    var errorMessage: String
        private set

    constructor(errorCode: Int, message: String) {
        this.errorCode = errorCode
        errorMessage = message
    }

    constructor(errorCode: Int, message: String, e: Throwable?) : super(e) {
        this.errorCode = errorCode
        errorMessage = message
    }

    internal interface Code {
        companion object {
            const val ERROR_CODE_DATA_PARSE = 20001
            const val ERROR_CODE_SEVER_ERROR = 20002
            const val ERROR_CODE_NET_ERROR = 20003
        }
    }

    companion object {
        val PARSE_ERROR = ApiException(Code.ERROR_CODE_DATA_PARSE, "数据解析出错")
        val SERVER_ERROR = ApiException(Code.ERROR_CODE_SEVER_ERROR, "服务器响应出错")
        val NET_ERROR = ApiException(Code.ERROR_CODE_NET_ERROR, "网络连接出错")
    }
}