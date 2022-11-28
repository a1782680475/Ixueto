package com.xktech.ixueto.model

data class AuthenticationBasis(
    val authenticationState: AuthenticationStateEnum,
    val authenticationInfo: AuthenticationInfo
)
