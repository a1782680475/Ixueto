package com.xktech.ixueto.model

import java.io.Serializable

enum class QuizStateEnum(val value: Short) : Serializable {
    NOT_QUIZ(-1),
    NOT_PASS(0),
    PASS(1),
    STUDY_RESET(2)
}