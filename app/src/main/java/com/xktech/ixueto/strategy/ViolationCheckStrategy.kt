package com.xktech.ixueto.strategy

import java.util.*

class ViolationCheckStrategy(
    val maxAllowCheckSeconds: Long?,
    val maxAllowViolatedNumber: Int?,
    var violateNumber: Int,
) {
    private var violationCheckTimer: Timer? = null
    private var violationCheckTimerTask: TimerTask? = null
    var currentCountDownMilliseconds = 0L
    private val checkInterval = 500L
    private lateinit var onViolated: (violateNumber: Int, isMax: Boolean) -> Unit
    private var _runing: Boolean = false
    fun setViolatedListener(listener: (violateNumber: Int, isMax: Boolean) -> Unit) {
        this.onViolated = listener
    }

    fun startCheck() {
        if (maxAllowCheckSeconds != null) {
            if (_runing) {
                return
            }
            _runing = true
            violationCheckTimer = Timer()
            violationCheckTimerTask = ViolationCheckTimerTask()
            violationCheckTimer?.schedule(
                violationCheckTimerTask,
                0, checkInterval
            )
        }
    }

    fun cancel() {
        _runing = false
        currentCountDownMilliseconds = 0L
        violationCheckTimerTask?.cancel()
        violationCheckTimer?.cancel()
    }

    inner class ViolationCheckTimerTask :
        TimerTask() {
        override fun run() {
            currentCountDownMilliseconds += checkInterval
            if (currentCountDownMilliseconds >= maxAllowCheckSeconds!! * 1000) {
                cancel()
                _runing = false
                currentCountDownMilliseconds = 0L
                violateNumber++
                var isMax = false
                if (violateNumber >= maxAllowViolatedNumber!!) {
                    isMax = true
                }
                onViolated(violateNumber, isMax)
            }
        }
    }
}