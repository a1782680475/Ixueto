package com.xktech.ixueto.model

data class QuizQuestionAnswerBrief(
    val Id: Int,
    val Type: String,
    val Question: String,
    val IsRight: Boolean?,
)
