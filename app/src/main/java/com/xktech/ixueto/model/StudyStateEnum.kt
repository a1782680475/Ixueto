package com.xktech.ixueto.model

import java.io.Serializable

enum class StudyStateEnum(val value: Short) : Serializable {
    NOT_STARTED(0),
    STUDYING(1),
    FINISHED(2);
}