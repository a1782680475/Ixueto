package com.xktech.ixueto.model

data class LoginResult(
    val WebToken: String,
    val AppToken: String,
    val DESUserInfo: Map<String,String>
)
