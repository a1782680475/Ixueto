package com.xktech.ixueto.model

data class AuthenticationBrieInfo(
    val AuthenticationState: Short,
    val Name: String,
    val IdCard: String,
    val Identity: String,
    val Unit: String,
    val Time: String,
    val FailReason: String
)