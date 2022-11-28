package com.xktech.ixueto.model

import java.io.Serializable

enum class PhotoTypeEnum(val value: Short): Serializable {
    ID_CARD_FRONT(0),
    ID_CARD_BACK(1),
    RECENT(2),
    STUDENT_STATUS(3),
    RESIDENCE_INDEX(4),
    RESIDENCE_DETAIL(5),
    DIPLOMA(6),
    STANDARD(7)
}