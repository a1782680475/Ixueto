package com.xktech.ixueto.model

enum class WithinStateEnum(val value: Short){
    DATE_LIMITED(0),
    UNLIMITED(1),
    TIME_OF_DAY_LIMITED(2),
    DATE_AND_TIME_LIMITED(3)
}
