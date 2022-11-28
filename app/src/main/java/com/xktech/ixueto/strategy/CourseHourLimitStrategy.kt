package com.xktech.ixueto.strategy

import java.util.*

class CourseHourLimitStrategy(
    private val maxAllowStudyCourseHoursEveryDay: Int?,
    private val minutesEveryCourseHours: Int,
    private val totalStudiedSecondsToday: Long,
    private val initStudySeconds: Long,
    private val nextDayMilliseconds: Long
) {
    var isLimitedStudy: Boolean = false
    private var daySpanTimer: Timer? = null
    private var daySpanTimerTask: TimerTask? = null
    private lateinit var onFirstCourseHourLimitChecked: (Boolean) -> Unit
    private lateinit var onLimitCourseHour: (Long) -> Unit
    private lateinit var onDaySpan: () -> Unit
    var surplusStudySecondsToday: Long = 0L
    private var allowMaxSeekSeconds: Long? = null

    fun init() {
        if (maxAllowStudyCourseHoursEveryDay != null) {
            surplusStudySecondsToday =
                maxAllowStudyCourseHoursEveryDay * minutesEveryCourseHours * 60 - totalStudiedSecondsToday!!
            allowMaxSeekSeconds = surplusStudySecondsToday + initStudySeconds
            if (surplusStudySecondsToday <= 0) {
                isLimitedStudy = true
                onFirstCourseHourLimitChecked(false)
            } else {
                onFirstCourseHourLimitChecked(true)
                daySpanTimer = Timer()
                daySpanTimerTask = DaySpanTimerTask()
                daySpanTimer!!.schedule(
                    daySpanTimerTask,
                    nextDayMilliseconds
                )
            }
        }else{
            onFirstCourseHourLimitChecked(true)
        }
    }

    fun setOnDaySpanListener(listener: () -> Unit) {
        this.onDaySpan = listener
    }

    fun setOnFirstCourseHourLimitCheckedListener(listener: (Boolean) -> Unit) {
        this.onFirstCourseHourLimitChecked = listener
    }

    fun setOnLimitCourseHourListener(listener: (Long) -> Unit) {
        this.onLimitCourseHour = listener
    }


    fun check(currentStudiedSecond: Long) {
        allowMaxSeekSeconds?.let {
            if (allowMaxSeekSeconds!! <= currentStudiedSecond) {
                isLimitedStudy = true
                onLimitCourseHour(currentStudiedSecond)
            }
        }
    }

    fun cancelTimer() {
        daySpanTimer?.let {
            daySpanTimerTask!!.cancel()
            daySpanTimer!!.cancel()
            daySpanTimerTask = null
            daySpanTimer = null
        }
    }


    inner class DaySpanTimerTask :
        TimerTask() {
        override fun run() {
            onDaySpan()
        }
    }
}