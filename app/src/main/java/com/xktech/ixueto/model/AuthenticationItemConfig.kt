package com.xktech.ixueto.model

import java.io.Serializable

data class AuthenticationItemConfig(
    /**
     * 控件类型（0-输入控件 1-下拉选择控件 2-图片上传控件）
     */
    val Type: Int,
    val Show: Boolean,
    val Editable: Boolean,
    val DefaultVerify: String,
    val Verify: String
) : Serializable