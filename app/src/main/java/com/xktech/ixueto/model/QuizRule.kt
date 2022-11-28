package com.xktech.ixueto.model

import java.io.Serializable

data class QuizRule(
    var NeedQuiz: Boolean,
    var QuizPassRule: Short,
    var MaxAllowSeconds: Long?,
    var QuestionNumber: Int?,
    var MaxAllowRetake: Int?,
    var LimitRetake: Boolean,
    var ResetStudyIfNotPass: Boolean,
) : Serializable