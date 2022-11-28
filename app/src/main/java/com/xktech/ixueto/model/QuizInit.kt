package com.xktech.ixueto.model

data class QuizInit(
    val NowTimeStamp: Long,
    val EndTimeStamp: Long?,
    val QuestionIds: List<Int>,
)
