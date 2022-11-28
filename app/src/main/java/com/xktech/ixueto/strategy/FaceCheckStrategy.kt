package com.xktech.ixueto.strategy

import com.xktech.ixueto.model.*
import com.xktech.ixueto.utils.EnumUtils.getFaceCheckTimeRule
import java.util.*
import kotlin.math.roundToLong

class FaceCheckStrategy(
    rule: Rule,
    private val courseInfo: CourseInfo,
    private val studyInfo: CourseStudyInfo,
    private val needOnceCheck: Boolean,
) {
    private var _currentStage: Stage? = null
        set(value) {
            currentStage = value
            field = value
        }
    var currentStage: Stage? = null
    private lateinit var onFaceCheck: (stage: Stage, needLivenessCheck: Boolean) -> Unit
    private var faceCheckTimer: Timer? = null
    private var faceCheckTimerTask: TimerTask? = null
    private var faceCheckRule: FaceCheckRule = rule.FaceCheckRule
    private var studyRule: StudyRule = rule.StudyRule
    private var needRandomCheckAtMiddle: Boolean = false
    private var randomCheckAtMiddleIsFinished: Boolean = false

    /**
     * 识别回调
     */
    fun setFaceCheckListener(listener: (stage: Stage, needLivenessCheck: Boolean) -> Unit) {
        this.onFaceCheck = listener
    }

    /**
     * 人脸检测（立即执行）
     */
    fun startFaceCheck(stage: Stage) {
        callBack(stage, needLivenessCheck(stage))
    }

    /**
     * 人脸检测（延迟执行）
     */
    fun startFaceCheckDelay(stage: Stage) {
        if (faceCheckRule.NeedCheck) {
            if (needRandomCheckAtMiddle && !randomCheckAtMiddleIsFinished) {
                val randSeconds: Long =
                    ((Random().nextInt((courseInfo.VideoSeconds - 20 + 1).toInt()) + 10) * 1000).toLong()
                start(randSeconds, stage)
                randomCheckAtMiddleIsFinished = true
            } else {
                when (getFaceCheckTimeRule(faceCheckRule.FaceCheckTimeRule)) {
                    FaceCheckTimeRuleEnum.FIXED_INTERVAL -> {
                        val delay: Long = faceCheckRule.CheckLimitSeconds * 1000.toLong()
                        start(delay, stage)
                    }
                    FaceCheckTimeRuleEnum.RANDOM_INTERVAL -> {
                        if (faceCheckRule.CheckNumberEveryCourseHours != null) {
                            var oneCheckLimitMinutes =
                                studyRule.MinutesEveryCourseHours / ((faceCheckRule.CheckNumberEveryCourseHours!!).toDouble())
                            val delay: Long = (oneCheckLimitMinutes * 60000).roundToLong()
                            start(delay, stage)
                        } else {
                            val delay: Long = 900 * 1000.toLong()
                            start(delay, stage)
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun cancelTimer() {
        faceCheckTimer?.cancel()
        faceCheckTimerTask?.cancel()
    }

    fun init() {
        if (faceCheckRule.NeedCheck) {
            if (studyInfo.IsFinished) {
                if (faceCheckRule.NeedCheckAtFinished && !studyInfo.LastFaceCheckFinished) {
                    startFaceCheck(Stage.AFTER)
                }
            } else {
                var faceCheckTimeRule = getFaceCheckTimeRule(faceCheckRule.FaceCheckTimeRule)
                when (faceCheckTimeRule) {
                    FaceCheckTimeRuleEnum.FIXED_INTERVAL,
                    FaceCheckTimeRuleEnum.RANDOM_INTERVAL -> {
                        startFaceCheck(Stage.BEFORE)
                    }
                    FaceCheckTimeRuleEnum.ONE_EVERY_DAY,
                    FaceCheckTimeRuleEnum.ONE_EVERY_COURSE,
                    FaceCheckTimeRuleEnum.ONE_EVERY_SUBJECT,
                    -> {
                        if (needOnceCheck) {
                            startFaceCheck(Stage.BEFORE)
                        }
                    }
                }
                when (faceCheckTimeRule) {
                    FaceCheckTimeRuleEnum.FIXED_INTERVAL -> {
                        if (faceCheckRule.NeedMustCheckAtMiddle) {
                            if (courseInfo.VideoSeconds <= faceCheckRule.CheckLimitSeconds) {
                                needRandomCheckAtMiddle = true
                            }
                        }
                    }
                    FaceCheckTimeRuleEnum.RANDOM_INTERVAL -> {
                        if (faceCheckRule.NeedMustCheckAtMiddle) {
                            if (faceCheckRule.CheckNumberEveryCourseHours == null) {
                                if (courseInfo.VideoSeconds <= 900 * 1000.toLong()) {
                                    needRandomCheckAtMiddle = true
                                }
                            }
                        }
                    }
                    else -> {
                        needRandomCheckAtMiddle = false
                    }
                }
            }
        }
    }

    private fun start(delay: Long, stage: Stage) {
        faceCheckTimer = Timer()
        faceCheckTimerTask = FaceCheckTimerTask(stage)
        faceCheckTimer!!.schedule(
            faceCheckTimerTask,
            delay
        )
    }

    private fun callBack(stage: Stage, needLivenessCheck: Boolean) {
        currentStage = stage
        onFaceCheck(stage, needLivenessCheck)
    }

    fun needLivenessCheck(stage: Stage): Boolean {
        if (!faceCheckRule.NeedLivenessCheck) {
            return false
        } else {
            return if (faceCheckRule.NeedLivenessCheckAtMiddle) {
                true
            } else {
                when (stage) {
                    Stage.STUDYING -> {
                        false
                    }
                    else -> {
                        true
                    }
                }
            }
        }
    }

    inner class FaceCheckTimerTask(private val stage: Stage) :
        TimerTask() {
        override fun run() {
            callBack(stage, needLivenessCheck(stage))
            cancel()
        }
    }

    /**
     * 人脸识别阶段
     */
    enum class Stage {
        //学习开始前
        BEFORE,

        //学习中
        STUDYING,

        //学习结束后
        AFTER
    }
}