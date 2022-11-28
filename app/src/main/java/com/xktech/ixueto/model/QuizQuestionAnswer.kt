package com.xktech.ixueto.model

data class QuizQuestionAnswer(
    val Type: String,
    val Question: String,
    val Options: String,
    val Answer: String,
    val AnswerReference: String,
    val Explains: String,
    val IsRight: Boolean?
)
