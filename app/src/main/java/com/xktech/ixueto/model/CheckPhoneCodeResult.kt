package com.xktech.ixueto.model

data class CheckPhoneCodeResult(
    var IsSuccess: Boolean,
    var ErrorMessage: String?,
    var Token: String
)
