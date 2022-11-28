package com.xktech.ixueto.model

data class QuizResult(
    val IsPass: Boolean,
    val QuestionNumber: Int,
    val AnswerNumber: Int,
    val RightAnswerNumber: Int,
)
