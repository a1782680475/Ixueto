package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import java.util.*

open class CountDownViewModel : ViewModel() {
    private var countDownTimer: Timer? = null
    private var countDownTimerTask: TimerTask? = null
    private lateinit var onCountDownChange: (countDownSeconds: Int, isFinished: Boolean) -> Unit
    var isRunning = false
    fun setOnCountDownChangeListener(listener: (Int,Boolean) -> Unit) {
        this.onCountDownChange = listener
    }
    var countDownSeconds = 0
    fun startCountDown() {
        isRunning = true
        countDownTimer = Timer()
        countDownTimerTask = CountDownTimerTask()
        countDownTimer?.schedule(
            countDownTimerTask,
            1000, 1000
        )
    }

    inner class CountDownTimerTask() :
        TimerTask() {
        override fun run() {
            countDownSeconds--
            var isFinished = false
            if (countDownSeconds <= 0) {
                isFinished = true
                isRunning = false
                cancel()
            }
            onCountDownChange(countDownSeconds, isFinished)
        }
    }
}