package com.xktech.ixueto.data.remote.entity.request

data class QuizAnswer(
    var ProfessionId: Int,
    var SubjectId: Int,
    var CourseId: Int,
    var QuestionId: Int?,
    var Answers: MutableList<String>,
)
