package com.xktech.ixueto.model

import java.io.Serializable

data class FaceCheckRule(
    var NeedCheck: Boolean,
    val FaceCheckTimeRule: Short,
    var CheckLimitSeconds: Int,
    val CheckNumberEveryCourseHours: Int?,
    val OneAtMorning: Boolean,
    val OneAtAfternoon: Boolean,
    val NeedCheckAtFinished: Boolean,
    val NeedMustCheckAtMiddle: Boolean,
    val NeedLivenessCheck: Boolean,
    val NeedLivenessCheckAtMiddle: Boolean,
    val MaxAllowCheckSeconds: Long?,
    val MaxAllowViolatedNumber: Int?
) : Serializable