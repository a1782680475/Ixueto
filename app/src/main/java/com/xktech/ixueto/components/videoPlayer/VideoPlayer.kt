package com.xktech.ixueto.components.videoPlayer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.AudioManager
import android.net.ConnectivityManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import cn.jzvd.JZDataSource
import cn.jzvd.JZUtils
import cn.jzvd.JzvdStd
import com.xktech.ixueto.R
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt


class VideoPlayer :
    JzvdStd {
    var initStudySeconds: Long = 0L
        set(value) {
            initStudyPosition = value * 1000
            field = value
        }
    var isAlertAtNotWifi: Boolean = true
    var isGesture: Boolean = true
    var gestureProcessRule: Int = 0
    var isAutoSetCompleteStateAtFinished: Boolean = false
    private var maxSeekPosition = 0L
    private var initStudyPosition: Long = 0L
        set(value) {
            maxSeekPosition = value
            field = value
        }
    
    var isRestudy = false
    var message: String? = null
        set(value) {
            if (value.isNullOrEmpty()) {
                this.messageTextView.visibility = GONE
                this.messageTextView.text = ""
            } else {
                this.autoMessageTextView.visibility = GONE
                this.messageTextView.visibility = VISIBLE
                this.messageTextView.text = value
                this.bottomContainer.visibility = GONE
            }
        }
    var isBannedPlay: Boolean = true
    private var onBackClick: ((Boolean) -> Unit)? = null
    private var onStudyTimeChange: ((Long) -> Unit)? = null
    private var onStartVideo: ((Boolean) -> Unit)? = null
    private var onVideoPlaying: ((Boolean) -> Unit)? = null
    private var onScreenStateChange: ((Boolean) -> Unit)? = null
    private var onReplay: ((Boolean) -> Unit)? = null
    private var _onCompletion: (() -> Unit)? = null
    private var onMoreButtonClick: (() -> Unit)? = null
    private lateinit var messageTextView: TextView
    private lateinit var maskView: View
    private lateinit var centerContainer: ViewGroup
    private lateinit var centerContainerImage: ImageView
    private lateinit var autoMessageTextView: TextView
    private lateinit var moreButton: ImageView
    private lateinit var centerContainerAnimator: ObjectAnimator
    private var autoMessageTimer: Timer? = null
    private var autoMessageTimerTask: TimerTask? = null
    private var WIFI_TIP_SHOWED = false
    private var screenHeight = 0
    private var screenWidth = 0
    private var navigationHeight = 0
    private var statusBarHeight = 0
    var startButtonState: StartButtonState = StartButtonState()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)

    override fun init(context: Context?) {
        super.init(context)
        initView()
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_player
    }

    private fun initView() {
        titleTextView.visibility = View.INVISIBLE
        messageTextView = findViewById(R.id.message)
        autoMessageTextView = findViewById(R.id.auto_message)
        maskView = findViewById(R.id.mask)
        centerContainer = findViewById(R.id.center_container)
        centerContainerImage = findViewById(R.id.center_container_image)
        moreButton = findViewById(R.id.more_button)
        centerContainer.setOnClickListener(this)
        centerContainerAnimator =
            ObjectAnimator.ofFloat(centerContainerImage, View.ROTATION, 0f, 360f)
        centerContainerAnimator.repeatCount = INFINITE
        centerContainerAnimator.interpolator = LinearInterpolator()
        centerContainerAnimator.duration = 11000
        WIFI_TIP_DIALOG_SHOWED = true
        screenHeight = JZUtils.getScreenHeight(context)
        screenWidth = JZUtils.getScreenHeight(context)
        navigationHeight = JZUtils.getNavigationBarHeight(context)
        statusBarHeight = JZUtils.getStatusBarHeight(context)
        moreButton.setOnClickListener {
            showPlayerDialog()
        }
        startButton.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    centerContainerImage.isPressed = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    centerContainerImage.isPressed = false
                }
            }
            false
        })
    }

    override fun setScreenFullscreen() {
        screen = SCREEN_FULLSCREEN
        fullscreenButton.setImageResource(R.drawable.pl_shrink)
        backButton.visibility = VISIBLE
        tinyBackImageView.visibility = INVISIBLE
        batteryTimeLayout.visibility = VISIBLE
        if (jzDataSource.urlsMap.size == 1) {
            clarity.visibility = GONE
        } else {
            clarity.text = jzDataSource.currentKey.toString()
            clarity.visibility = VISIBLE
        }
        changeStartButtonSize(
            resources.getDimension(R.dimen.jz_start_button_w_fullscreen).toInt()
        )
        changeLoadingSize(resources.getDimension(R.dimen.jz_loading_w_h_fullscreen).toInt())
        changeCenterContainerImageSize(
            resources.getDimension(R.dimen.jz_center_container_w_h_fullscreen).toInt()
        )
        setSystemTimeAndBattery()
        changeAutoMessageTextViewSize(16f)
        changeMessageTextViewSize(16f)
        val autoMessageTextLp =
            (autoMessageTextView.layoutParams as RelativeLayout.LayoutParams).apply {
                bottomMargin = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 65f).toInt()
            }
        autoMessageTextView.layoutParams = autoMessageTextLp
        val paddingTop = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 10f).toInt()
        val paddingLeft = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 11f).toInt()
        autoMessageTextView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop)
        val messageTextLp = (messageTextView.layoutParams as RelativeLayout.LayoutParams).apply {
            bottomMargin = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 65f).toInt()
        }
        messageTextView.layoutParams = messageTextLp
        messageTextView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop)
        val bottomContainerLp =
            (bottomContainer.layoutParams as RelativeLayout.LayoutParams).apply {
                height = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 70f).toInt()
            }
        bottomContainer.layoutParams = bottomContainerLp
        moreButton.visibility = View.GONE
        val moreButtonLp = moreButton.layoutParams as RelativeLayout.LayoutParams
        moreButtonLp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        if (isRestudy) {
            replayTextView.visibility = VISIBLE
        }
        onScreenStateChange?.let { it(true) }
    }

    override fun setScreenNormal() {
        screen = SCREEN_NORMAL
        fullscreenButton.setImageResource(R.drawable.pl_enlarge)
        //backButton.visibility = GONE
        tinyBackImageView.visibility = INVISIBLE
        changeStartButtonSize(
            resources.getDimension(R.dimen.jz_start_button_w_normal).toInt()
        )
        changeLoadingSize(resources.getDimension(R.dimen.jz_loading_w_h_normal).toInt())
        changeCenterContainerImageSize(
            resources.getDimension(R.dimen.jz_center_container_w_h_normal).toInt()
        )
        batteryTimeLayout.visibility = GONE
        clarity.visibility = GONE
        changeAutoMessageTextViewSize(12f)
        changeMessageTextViewSize(12f)
        val lp = (autoMessageTextView.layoutParams as RelativeLayout.LayoutParams).apply {
            bottomMargin = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 45f).toInt()
        }
        autoMessageTextView.layoutParams = lp
        val paddingTop = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 8f).toInt()
        val paddingLeft = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 9f).toInt()
        autoMessageTextView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop)
        val messageTextLp = (messageTextView.layoutParams as RelativeLayout.LayoutParams).apply {
            bottomMargin = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 45f).toInt()
        }
        messageTextView.layoutParams = messageTextLp
        messageTextView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop)
        val bottomContainerLp =
            (bottomContainer.layoutParams as RelativeLayout.LayoutParams).apply {
                height = com.xktech.ixueto.utils.DimenUtils.dp2px(context, 60f).toInt()
            }
        bottomContainer.layoutParams = bottomContainerLp
        moreButton.visibility = View.VISIBLE
        val moreButtonLp = moreButton.layoutParams as RelativeLayout.LayoutParams
        moreButtonLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        if (isRestudy) {
            replayTextView.visibility = GONE
        }
        onScreenStateChange?.let { it(false) }
    }

    override fun clickBack() {
        var isFullScreen = screen == SCREEN_FULLSCREEN
        if (isFullScreen) {
            super.clickBack()
        }
        onBackClick?.let { it(isFullScreen) }
    }

    var timeProgressBarRule: TimeProgressBarRule = TimeProgressBarRule.NORMAL

    override fun setUp(url: String?, title: String?, screen: Int, mediaInterfaceClass: Class<*>?) {
        val jzDataSource = JZDataSource(url, title)
        jzDataSource.headerMap["referer"] = "https://www.ixueto.com"
        setUp(JZDataSource(url, title), screen, mediaInterfaceClass)
    }

    override fun touchActionMove(x: Float, y: Float) {
        if (isGesture) {
            Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ")
            val deltaX = x - mDownX
            var deltaY = y - mDownY
            val absDeltaX = abs(deltaX)
            val absDeltaY = abs(deltaY)
            if (screen == SCREEN_FULLSCREEN) {
                //拖动的是NavigationBar和状态栏
                if (mDownX > screenHeight - navigationHeight || mDownX < statusBarHeight || mDownY < statusBarHeight || mDownY > screenWidth - navigationHeight
                ) {
                    return
                }
                if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                    if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                        cancelProgressTimer()
                        if (absDeltaX >= THRESHOLD) {
                            if (state != STATE_ERROR) {
                                mChangePosition = true
                                mGestureDownPosition = currentPositionWhenPlaying
                            }
                        } else {
                            if (mDownX < mScreenHeight * 0.5f) {
                                mChangeBrightness = true
                                val lp = JZUtils.getWindow(context).attributes
                                if (lp.screenBrightness < 0) {
                                    try {
                                        mGestureDownBrightness =
                                            Settings.System.getInt(
                                                context.contentResolver,
                                                Settings.System.SCREEN_BRIGHTNESS
                                            ).toFloat()
                                        Log.i(
                                            TAG,
                                            "current system brightness: $mGestureDownBrightness"
                                        )
                                    } catch (e: SettingNotFoundException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    mGestureDownBrightness = lp.screenBrightness * 255
                                    Log.i(
                                        TAG,
                                        "current activity brightness: $mGestureDownBrightness"
                                    )
                                }
                            } else {
                                mChangeVolume = true
                                mGestureDownVolume =
                                    mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            }
                        }
                    }
                }
            }
            if (timeProgressBarRule == TimeProgressBarRule.SEEK_BAN) {
                mChangePosition = false
            }
            if (mChangePosition) {
                val totalTimeDuration = duration
                if (PROGRESS_DRAG_RATE <= 0) {
                    Log.d(TAG, "error PROGRESS_DRAG_RATE value")
                    PROGRESS_DRAG_RATE = 1f
                }
                var toSeekPosition = when (gestureProcessRule) {
                    0 -> {
                        (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenHeight * PROGRESS_DRAG_RATE)).toLong()
                    }
                    1 -> {
                        (mGestureDownPosition + ((deltaX / mScreenHeight) * 50000).toLong())
                    }
                    else -> {
                        (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenHeight * PROGRESS_DRAG_RATE)).toLong()
                    }
                }
                if (toSeekPosition > totalTimeDuration) toSeekPosition = totalTimeDuration
                when (timeProgressBarRule) {
                    TimeProgressBarRule.NORMAL -> {
                        mSeekTimePosition = toSeekPosition

                    }
                    TimeProgressBarRule.AFTER_SEEK_BAN -> {
                        mSeekTimePosition = if (toSeekPosition <= maxSeekPosition) {
                            toSeekPosition
                        } else {
                            maxSeekPosition
                        }
                    }
                    else -> {
                        mSeekTimePosition = toSeekPosition
                    }
                }
                val seekTime = JZUtils.stringForTime(mSeekTimePosition)
                val totalTime = JZUtils.stringForTime(totalTimeDuration)
                showProgressDialog(
                    deltaX,
                    seekTime,
                    mSeekTimePosition,
                    totalTime,
                    totalTimeDuration
                )
            }
            if (mChangeVolume) {
                deltaY = -deltaY
                val max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val deltaV = (max * deltaY * 3 / mScreenHeight).toInt()
                mAudioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    mGestureDownVolume + deltaV,
                    0
                )
                val volumePercent =
                    (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight).toInt()
                showVolumeDialog(-deltaY, volumePercent)
            }
            if (mChangeBrightness) {
                deltaY = -deltaY
                val deltaV = (255 * deltaY * 3 / mScreenHeight).toInt()
                val params = JZUtils.getWindow(context).attributes
                if ((mGestureDownBrightness + deltaV) / 255 >= 1) {
                    params.screenBrightness = 1f
                } else if ((mGestureDownBrightness + deltaV) / 255 <= 0) {
                    params.screenBrightness = 0.01f
                } else {
                    params.screenBrightness = (mGestureDownBrightness + deltaV) / 255
                }
                JZUtils.getWindow(context).attributes = params
                val brightnessPercent =
                    (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight).toInt()
                showBrightnessDialog(brightnessPercent)
            }
        }
    }

    override fun onProgress(progress: Int, position: Long, duration: Long) {
        super.onProgress(progress, position, duration)
        onStudyTimeChange?.let { it(position) }
        maxSeekPosition = when (timeProgressBarRule) {
            TimeProgressBarRule.AFTER_SEEK_BAN -> {
                if (maxSeekPosition > initStudyPosition && maxSeekPosition > position)
                    maxSeekPosition
                else if (initStudyPosition > position)
                    initStudyPosition
                else
                    position
            }
            else -> {
                position
            }
        }
    }

    override fun onStatePlaying() {
        onVideoPlaying?.let { it(isBannedPlay) }
        if (!isBannedPlay && !isRestudy) {
            when (state) {
                STATE_PREPARED -> {
                    super.onStatePlaying()
                    centerContainer.visibility = INVISIBLE
                    checkWifiState()
                }
                else -> {
                    Log.i(TAG, "onStatePlaying " + " [" + this.hashCode() + "] ")
                    state = STATE_PLAYING
                    startProgressTimer()
                    updateStartImage()
                    startDismissControlViewTimer()
                }
            }
        }
    }


    private fun initProgress() {
        seekToInAdvance = maxSeekPosition
    }

    fun setProgress(seconds: Long) {
        seekToInAdvance = (seconds * 1000)
    }


    override fun startVideo() {
        onStartVideo?.let { it(isBannedPlay) }
        if (!isBannedPlay) {
            initProgress()
            if (timeProgressBarRule == TimeProgressBarRule.SEEK_BAN) {
                progressBar.isEnabled = false
                progressBar.isClickable = false
            }
            super.startVideo()
        }
    }


    override fun onStartTrackingTouch(seekBar: SeekBar) {
        super.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        when (timeProgressBarRule) {
            TimeProgressBarRule.NORMAL -> {
                super.onStopTrackingTouch(seekBar)
            }
            TimeProgressBarRule.AFTER_SEEK_BAN -> {
                val toSeekPosition = seekBar!!.progress * duration / 100
                if (toSeekPosition > maxSeekPosition) {
                    seekToManulPosition = (maxSeekPosition * 100 / duration).toInt()
                    mediaInterface.seekTo(maxSeekPosition)
                    UPDATE_PROGRESS_TIMER = Timer()
                    mProgressTimerTask = ProgressTimerTask()
                    UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300)
                } else {
                    super.onStopTrackingTouch(seekBar)
                }
            }
            else -> {

            }
        }
    }

    override fun showWifiDialog() {

    }

    private fun checkWifiState() {
        if (!jzDataSource.currentUrl.toString().startsWith("file") && !
            jzDataSource.currentUrl.toString().startsWith("/") &&
            !JZUtils.isWifiConnected(jzvdContext) && !WIFI_TIP_SHOWED
        ) {
            showAutoMessage()
        }
    }

    private fun showAutoMessage() {
        if (isAlertAtNotWifi) {
            WIFI_TIP_SHOWED = true
            autoMessageTextView.visibility = View.VISIBLE
            startAutoMessageViewTimer()
        }
    }

    fun messageHidden() {
        this.messageTextView.visibility = GONE
    }

    fun pause() {
        if (mediaInterface != null) {
            mediaInterface.pause()
            super.onStatePause()
        }
    }

    fun play() {
        if (state == STATE_PAUSE) {
            mediaInterface.start()
            dissmissControlView()
            state = STATE_PLAYING
            updateStartImage()
        } else {
            clickStart()
            centerContainer.visibility = INVISIBLE
        }
    }

    fun setStudyTimeChangeListener(listener: (Long) -> Unit) {
        this.onStudyTimeChange = listener
    }

    fun setOnBackClickListener(listener: (Boolean) -> Unit) {
        this.onBackClick = listener
    }

    fun setStartVideoListener(listener: (Boolean) -> Unit) {
        this.onStartVideo = listener
    }

    fun setVideoPlayingListener(listener: (Boolean) -> Unit) {
        this.onVideoPlaying = listener
    }

    fun setScreenStateChangeListener(listener: (Boolean) -> Unit) {
        this.onScreenStateChange = listener
    }

    fun setCompletionListener(listener: () -> Unit) {
        this._onCompletion = listener
    }

    fun setOnReplayListener(listener: (Boolean) -> Unit) {
        this.onReplay = listener
    }

    fun setOnMoreButtonClickListener(listener: () -> Unit) {
        this.onMoreButtonClick = listener
    }

    fun setStartButtonIcon(icon: Int) {
        this.startButton.setImageResource(icon)
    }

    override fun onCompletion() {
        super.onCompletion()
        if (screen == SCREEN_FULLSCREEN) {
            super.setScreenNormal()
        }
        this._onCompletion?.let { it() }
    }

    fun resetViewForRestudy() {
        cancelDismissControlViewTimer()
        cancelProgressTimer()
        dismissBrightnessDialog()
        dismissProgressDialog()
        dismissVolumeDialog()
        bottomContainer.visibility = INVISIBLE
        state = STATE_AUTO_COMPLETE
        maskView.visibility = VISIBLE
        centerContainer.visibility = VISIBLE
        startButton.visibility = VISIBLE
        startButton.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.avd_face_id_to_replay
            )
        )
        val imgDrawable = startButton.drawable
        if (imgDrawable is AnimatedVectorDrawableCompat) {
            imgDrawable.start()
        } else if (imgDrawable is AnimatedVectorDrawable) {
            imgDrawable.start()
        }
        replayTextView.text = "重新学习"
        replayTextView.visibility = VISIBLE
        if (centerContainerAnimator.isRunning) {
            centerContainerAnimator.cancel()
        }
        isRestudy = true
    }

    fun resetViewForBanStudy() {
        cancelDismissControlViewTimer()
        cancelProgressTimer()
        dismissBrightnessDialog()
        dismissProgressDialog()
        dismissVolumeDialog()
        bottomContainer.visibility = INVISIBLE
        centerContainer.visibility = INVISIBLE
    }

    fun getState(): Int {
        return this.state
    }

    private fun changeMessageTextViewSize(size: Float) {
        messageTextView.textSize = size
    }

    private fun changeAutoMessageTextViewSize(size: Float) {
        autoMessageTextView.textSize = size
    }

    override fun gotoFullscreen() {
        super.gotoFullscreen()
        if (titleTextView != null) {
            titleTextView.visibility = View.VISIBLE
        }
    }

    override fun gotoNormalScreen() {
        super.gotoNormalScreen()
        if (titleTextView != null) {
            titleTextView.visibility = INVISIBLE
        }
    }

    override fun startDismissControlViewTimer() {
        cancelDismissControlViewTimer()
        DISMISS_CONTROL_VIEW_TIMER = Timer()
        mDismissControlViewTimerTask = DismissControlViewTimerTask()
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, 3000)
    }

    override fun dissmissControlView() {
        if (state != STATE_NORMAL && state != STATE_ERROR && state != STATE_AUTO_COMPLETE) {
            post {
                bottomContainer.visibility = INVISIBLE
                topContainer.visibility = INVISIBLE
                centerContainer.visibility = INVISIBLE
                maskView.visibility = INVISIBLE
                if (screen != SCREEN_TINY) {
                    bottomProgressBar.visibility = VISIBLE
                }
            }
        }
    }

    override fun updateStartImage() {
        when (state) {
            STATE_PLAYING -> {
                maskView.visibility = VISIBLE
                centerContainer.visibility = VISIBLE
                if (!centerContainerAnimator.isStarted) {
                    centerContainerAnimator.start()
                } else {
                    centerContainerAnimator.resume()
                }
                startButton.visibility = VISIBLE
                if (!startButtonState.isPreFaceCheck && startButtonState.playState != PlayState.PLAYING) {
                    startButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.avd_play_to_pause
                        )
                    )
                    val imgDrawable = startButton.drawable
                    if (imgDrawable is AnimatedVectorDrawableCompat) {
                        imgDrawable.start()
                    } else if (imgDrawable is AnimatedVectorDrawable) {
                        imgDrawable.start()
                    }
                }
                startButtonState.playState = PlayState.PLAYING
                replayTextView.visibility = GONE
            }
            STATE_ERROR -> {
                maskView.visibility = VISIBLE
                centerContainer.visibility = INVISIBLE
                startButton.visibility = INVISIBLE
                replayTextView.visibility = GONE
                mRetryLayout.visibility = VISIBLE
            }
            STATE_AUTO_COMPLETE -> {
                if (isRestudy || isAutoSetCompleteStateAtFinished) {
                    maskView.visibility = VISIBLE
                    centerContainer.visibility = VISIBLE
                    startButton.visibility = VISIBLE
                    startButton.setImageResource(R.drawable.ic_qp_replay)
                    if (!isRestudy || screen == SCREEN_FULLSCREEN) {
                        replayTextView.visibility = VISIBLE
                    }
                    if (centerContainerAnimator.isRunning) {
                        centerContainerAnimator.cancel()
                    }
                } else {
                    if (centerContainerAnimator.isRunning) {
                        centerContainerAnimator.cancel()
                    }
                }
            }
            else -> {
                if (!startButtonState.isPreFaceCheck && startButtonState.playState != PlayState.PAUSE) {
                    startButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.avd_pause_to_play
                        )
                    )
                    val imgDrawable = startButton.drawable
                    if (imgDrawable is AnimatedVectorDrawableCompat) {
                        imgDrawable.start()
                    } else if (imgDrawable is AnimatedVectorDrawable) {
                        imgDrawable.start()
                    }
                }
                startButtonState.playState = PlayState.PAUSE
                if (centerContainerAnimator.isRunning) {
                    centerContainerAnimator.pause()
                }
                replayTextView.visibility = GONE
            }
        }
    }

    override fun setAllControlsVisiblity(
        topCon: Int, bottomCon: Int, startBtn: Int, loadingPro: Int,
        posterImg: Int, bottomPro: Int, retryLayout: Int
    ) {
        if (startBtn == VISIBLE || loadingPro == VISIBLE) {
            maskView.visibility = VISIBLE
        } else {
            maskView.visibility = GONE

        }
        if (startBtn == VISIBLE || retryLayout == VISIBLE) {
            centerContainer.visibility = VISIBLE
        } else {
            centerContainer.visibility = GONE
        }
        super.setAllControlsVisiblity(
            topCon,
            bottomCon,
            startBtn,
            loadingPro,
            posterImg,
            bottomPro,
            retryLayout
        )
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.center_container) {
            clickStart()
        } else {
            super.onClick(v)
        }
    }

    override fun clickStart() {
        Log.i(TAG, "onClick start [" + this.hashCode() + "] ")
        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.currentUrl == null) {
            Toast.makeText(context, resources.getString(R.string.no_url), Toast.LENGTH_SHORT).show()
            return
        }
        when (state) {
            STATE_NORMAL -> {
                if (!jzDataSource.currentUrl.toString()
                        .startsWith("file") && !jzDataSource.currentUrl.toString()
                        .startsWith("/") &&
                    !JZUtils.isWifiConnected(context) && !WIFI_TIP_DIALOG_SHOWED
                ) { //这个可以放到std中
                    showWifiDialog()
                    return
                }
                startVideo()
            }
            STATE_PLAYING -> {
                Log.d(TAG, "pauseVideo [" + this.hashCode() + "] ")
                mediaInterface.pause()
                onStatePause()
            }
            STATE_PAUSE -> {
                if (!isBannedPlay) {
                    mediaInterface.start()
                }
                onStatePlaying()
            }
            STATE_AUTO_COMPLETE -> {
                if (!isRestudy) {
                    maxSeekPosition = 0
                    if (!isBannedPlay) {
                        startButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.avd_replay_to_play
                            )
                        )
                        val imgDrawable = startButton.drawable
                        if (imgDrawable is AnimatedVectorDrawableCompat) {
                            imgDrawable.start()
                        } else if (imgDrawable is AnimatedVectorDrawable) {
                            imgDrawable.start()
                        }
                    }
                    startVideo()
                    onReplay?.let { it(false) }
                } else {
                    isRestudy = false
                    state = STATE_NORMAL
                    maxSeekPosition = 0
                    mCurrentPosition = 0
                    resetProgressAndTime()
                    initStudySeconds = 0
                    mediaInterface?.seekTo(0)
                    startButtonState = StartButtonState()
                    startButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.avd_replay_to_play
                        )
                    )
                    replayTextView.visibility = GONE
                    val imgDrawable = startButton.drawable
                    if (imgDrawable is AnimatedVectorDrawableCompat) {
                        imgDrawable.start()
                    } else if (imgDrawable is AnimatedVectorDrawable) {
                        imgDrawable.start()
                    }
                    onReplay?.let { it(true) }
                }
            }
        }
    }

    override fun changeStartButtonSize(width: Int) {
        var newWidth = width
        var newHeight = (newWidth * (86.459 / 69.167)).toInt()
        var lp = startButton.layoutParams
        lp.height = newWidth
        lp.width = newHeight
    }

    private fun changeLoadingSize(size: Int) {
        var lp = loadingProgressBar.layoutParams
        lp.height = size
        lp.width = size
    }

    private fun changeCenterContainerImageSize(size: Int) {
        var lp = centerContainerImage.layoutParams
        lp.height = size
        lp.width = size
        var clp = centerContainer.layoutParams
        val cWidth = ceil(size * sqrt(2.0)).toInt()
        clp.width = cWidth
        clp.height = cWidth
        centerContainerImage.pivotX = (size / 2).toFloat()
        centerContainerImage.pivotY = (size / 2).toFloat()
    }

    private fun startAutoMessageViewTimer() {
        cancelAutoMessageViewTimer()
        autoMessageTimer = Timer()
        autoMessageTimerTask = AutoMessageTimerTask()
        autoMessageTimer!!.schedule(autoMessageTimerTask, 2500)
    }

    private fun cancelAutoMessageViewTimer() {
        autoMessageTimer?.cancel()
        autoMessageTimerTask?.cancel()
    }

    private var myWifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                val isWifi = JZUtils.isWifiConnected(context)
                if (mIsWifi == isWifi) return
                mIsWifi = isWifi
                if (!mIsWifi && !WIFI_TIP_SHOWED && state == STATE_PLAYING) {
                    checkWifiState()
                }
            }
        }
    }

    fun setFaceCheckState() {
        if (startButtonState.isFaceChecking) {
            return
        }
        startButtonState.isFaceChecking = true
        when (startButtonState.playState) {
            PlayState.PAUSE -> {
                startButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.avd_play_to_face_id
                    )
                )
            }
            PlayState.PLAYING -> {
                startButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.avd_pause_to_face_id
                    )
                )
            }
        }
        val imgDrawable = startButton.drawable
        if (imgDrawable is AnimatedVectorDrawableCompat) {
            imgDrawable.start()
        } else if (imgDrawable is AnimatedVectorDrawable) {
            imgDrawable.start()
        }
    }

    fun cancelFaceCheckState() {
        startButton.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.avd_face_id_to_pause
            )
        )
        val imgDrawable = startButton.drawable
        if (imgDrawable is AnimatedVectorDrawableCompat) {
            imgDrawable.start()
        } else if (imgDrawable is AnimatedVectorDrawable) {
            imgDrawable.start()
        }
    }

    fun cancelFaceCheckStateAtFinished() {
        maskView.visibility = VISIBLE
        centerContainer.visibility = VISIBLE
        startButton.visibility = VISIBLE
        replayTextView.visibility = VISIBLE
        if (centerContainerAnimator.isRunning) {
            centerContainerAnimator.cancel()
        }
        startButton.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.avd_face_id_to_replay
            )
        )
        val imgDrawable = startButton.drawable
        if (imgDrawable is AnimatedVectorDrawableCompat) {
            imgDrawable.start()
        } else if (imgDrawable is AnimatedVectorDrawable) {
            imgDrawable.start()
        }
    }

    private fun showPlayerDialog() {
        onMoreButtonClick?.let { it() }
    }

    override fun registerWifiListener(context: Context?) {
        if (context == null) return
        mIsWifi = JZUtils.isWifiConnected(context)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(myWifiReceiver, intentFilter)
    }

    override fun unregisterWifiListener(context: Context?) {
        if (context == null) return
        try {
            context.unregisterReceiver(myWifiReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    inner class AutoMessageTimerTask() :
        TimerTask() {
        override fun run() {
            cancelAutoMessageViewTimer()
            this@VideoPlayer.post {
                autoMessageTextView.visibility = View.GONE
            }
        }
    }

    //时间进度条规则
    enum class TimeProgressBarRule {
        //默认，允许拖拽
        NORMAL,

        //禁用拖拽
        SEEK_BAN,

        //禁用向后拖拽
        AFTER_SEEK_BAN
    }

}
