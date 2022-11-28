package com.xktech.ixueto.model

data class QuizQuestion(
    var Id: Int,
    val Type: String,
    val Question: String,
    val OptionNumber: Int?,
    val Options: String,
    val Answer: String
)
