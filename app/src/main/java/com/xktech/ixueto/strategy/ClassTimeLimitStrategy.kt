package com.xktech.ixueto.strategy

import com.xktech.ixueto.data.local.converter.DateConverter
import com.xktech.ixueto.model.ClassRule
import java.util.*

class ClassTimeLimitStrategy(private val classRule: ClassRule, var timeStamp: Long) {
    private lateinit var onClassTimeLimit: (String) -> Unit
    private lateinit var onFirstClassTimeLimitChecked: (Boolean) -> Unit
    private var classTimeLimitTimer: Timer? = null
    private var classTimeLimitTimerTask: TimerTask? = null
    fun setOnClassTimeLimitListener(listener: (String) -> Unit) {
        this.onClassTimeLimit = listener
    }

    fun setOnFirstClassTimeLimitCheckedListener(listener: (Boolean) -> Unit) {
        this.onFirstClassTimeLimitChecked = listener
    }

    fun init() {
        val result = checkAll()
        cancelTimer()
        if (result.first) {
            startTimer()
        } else {
            onClassTimeLimit(result.second)
        }
        onFirstClassTimeLimitChecked(result.first)

    }

    private fun startTimer() {
        classRule.EndTimeEveryDay?.let {
            classTimeLimitTimer = Timer()
            classTimeLimitTimerTask = ClassTimeLimitTimerTask()
            classTimeLimitTimer!!.schedule(
                classTimeLimitTimerTask,
                classRule.EndTimeEveryDay - timeStamp
            )
        }
    }

    fun cancelTimer() {
        classTimeLimitTimer?.let {
            classTimeLimitTimerTask!!.cancel()
            classTimeLimitTimer!!.cancel()
            classTimeLimitTimerTask = null
            classTimeLimitTimer = null
        }
    }

    private fun checkAll(): Pair<Boolean, String> {
        var isWithin = true
        var notAtWithinReason = ""
        classRule.StartTime?.let {
            if (isWithin && timeStamp <= classRule.StartTime) {
                isWithin = false
                notAtWithinReason =
                    "未到该班次学习时间（${DateConverter.converterDate(classRule.StartTime)}至${
                        if (classRule.EndTime != null) DateConverter.converterDate(classRule.EndTime) else "-"
                    }）"
            }
        }
        classRule.EndTime?.let {
            if (isWithin && timeStamp >= classRule.EndTime) {
                isWithin = false
                notAtWithinReason =
                    "已过该班次学习时间（${
                        if (classRule.StartTime != null) DateConverter.converterDate(classRule.StartTime) else "-"
                    }至${DateConverter.converterDate(classRule.EndTime)}）"
            }
        }
        classRule.StartTimeEveryDay?.let {
            if (isWithin && timeStamp <= classRule.StartTimeEveryDay) {
                isWithin = false
                notAtWithinReason = "未到学习时间（每日${
                    DateConverter.converterDate(
                        classRule.StartTimeEveryDay,
                        "HH:mm:ss"
                    )
                }至${
                    if (classRule.EndTimeEveryDay != null) DateConverter.converterDate(
                        classRule.EndTimeEveryDay,
                        "HH:mm:ss"
                    ) else "-"
                }）"
            }
        }
        classRule.EndTimeEveryDay?.let {
            if (isWithin && timeStamp >= classRule.EndTimeEveryDay) {
                isWithin = false
                notAtWithinReason = "已过学习时间（每日${
                    if (classRule.StartTimeEveryDay != null) DateConverter.converterDate(
                        classRule.StartTimeEveryDay,
                        "HH:mm:ss"
                    ) else "-"
                }至${
                    DateConverter.converterDate(
                        classRule.EndTimeEveryDay,
                        "HH:mm:ss"
                    )
                }）"
            }
        }
        return Pair(isWithin, notAtWithinReason)
    }

    fun checkClassEndTime() {
        classRule.EndTime?.let {
            if (classRule.EndTime - Date().time <= 0) {
                onClassTimeLimit(
                    "已过该班次学习时间（${
                        if (classRule.StartTime != null) DateConverter.converterDate(classRule.StartTime) else "-"
                    }至${DateConverter.converterDate(classRule.EndTime!!)}）"
                )
            }
        }
    }

    inner class ClassTimeLimitTimerTask() :
        TimerTask() {
        override fun run() {
            timeStamp = classRule.EndTimeEveryDay!!
            val result = checkAll()
            if (!result.first) {
                cancelTimer()
                onClassTimeLimit(result.second)
            }
        }
    }
}