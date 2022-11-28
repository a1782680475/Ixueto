package com.xktech.ixueto.model

import java.io.Serializable

data class Rule(
    val StudyRule: StudyRule,
    val FaceCheckRule: FaceCheckRule,
    var QuizRule: QuizRule
) : Serializable
