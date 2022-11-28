package com.xktech.ixueto.model

enum class FaceCheckTimeRuleEnum (val value: Short) {
    FIXED_INTERVAL(0),
    RANDOM_INTERVAL(1),
    ONE_EVERY_DAY(2),
    ONE_EVERY_COURSE(3),
    ONE_EVERY_SUBJECT(4),
}