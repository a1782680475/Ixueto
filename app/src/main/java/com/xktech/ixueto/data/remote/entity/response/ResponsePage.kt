package com.xktech.ixueto.data.remote.entity.response

data class ResponsePage<T>(
    var Rows: MutableList<T>,
    var Total: Int = 0
)