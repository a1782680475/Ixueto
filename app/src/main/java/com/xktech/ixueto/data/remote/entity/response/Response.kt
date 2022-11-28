package com.xktech.ixueto.data.remote.entity.response

class Response<T> {
    var code: Int = 200
    var msg: String = ""
    var data: T? = null
}