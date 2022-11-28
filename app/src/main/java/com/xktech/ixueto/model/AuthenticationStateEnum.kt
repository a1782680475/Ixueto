package com.xktech.ixueto.model

enum class AuthenticationStateEnum (val value: Short) {
    UN_AUTHENTICATION(0),
    AUTHENTICATED(1),
    AUTHENTICATING(2),
    AUTHENTICATE_FAILED(3),
    AUTHENTICATE_FOR_ARTIFICIAL(4)
}