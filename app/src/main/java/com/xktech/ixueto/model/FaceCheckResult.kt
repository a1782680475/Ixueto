package com.xktech.ixueto.model

data class FaceCheckResult(
    val Success: Boolean,
    val ErrorMessage: String,
    val AllowSave: Boolean,
)
