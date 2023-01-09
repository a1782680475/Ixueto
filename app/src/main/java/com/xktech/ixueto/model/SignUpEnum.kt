package com.xktech.ixueto.model

import java.io.Serializable

enum class SignUpEnum(val value: Short): Serializable {
    SIGNUP_FAIL(0),
    SIGNUP_SUCCESS(1),
    SIGNUP_EXAMINE(2),
}