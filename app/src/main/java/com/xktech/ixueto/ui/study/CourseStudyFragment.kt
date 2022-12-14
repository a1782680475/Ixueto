package com.xktech.ixueto.ui.study

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import cn.jzvd.JZMediaSystem
import cn.jzvd.Jzvd
import cn.jzvd.Jzvd.SCREEN_FULLSCREEN
import cn.jzvd.Jzvd.SCREEN_NORMAL
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.xktech.ixueto.BuildConfig
import com.xktech.ixueto.MyApplication
import com.xktech.ixueto.NavGraphDirections
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.StudyFragmentStateAdapter
import com.xktech.ixueto.components.PlayerDialogFragment
import com.xktech.ixueto.components.player.mediaInterface.JZMediaAliyun
import com.xktech.ixueto.components.videoPlayer.VideoPlayer
import com.xktech.ixueto.databinding.FragmentCourseStudyBinding
import com.xktech.ixueto.datastore.VideoPlay
import com.xktech.ixueto.model.*
import com.xktech.ixueto.strategy.ClassTimeLimitStrategy
import com.xktech.ixueto.strategy.CourseHourLimitStrategy
import com.xktech.ixueto.strategy.FaceCheckStrategy
import com.xktech.ixueto.strategy.ViolationCheckStrategy
import com.xktech.ixueto.ui.face.FaceAuthActivity
import com.xktech.ixueto.utils.EnumUtils
import com.xktech.ixueto.viewModel.CourseStudyFragmentViewModel
import com.xktech.ixueto.viewModel.NoticeViewModel
import com.xktech.ixueto.viewModel.SettingViewModel
import com.xktech.ixueto.viewModel.StudyViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.floor
import kotlin.math.round

@AndroidEntryPoint
class CourseStudyFragment : Fragment() {
    private var isFirstLoad = true
    private var rootView: View? = null
    private var binding: FragmentCourseStudyBinding? = null
    private var faceCheckStrategy: FaceCheckStrategy? = null
    private var violationCheckStrategy: ViolationCheckStrategy? = null
    private var courseHourLimitStrategy: CourseHourLimitStrategy? = null
    private var classTimeLimitStrategy: ClassTimeLimitStrategy? = null
    private var currentPlayedSeconds: Long = 0L
    private val localDBSaveLimitSeconds = 10
    private val remoteDBSaveLimitSeconds = 180
    private lateinit var player: VideoPlayer
    private var subjectId: Int? = null
    private var classId: Int? = null
    private var coursePosition: Int? = null
    private var courseId: Int? = null
    private var courseName: String? = null
    private var videoPoster: String? = null
    private var panelMain: ViewGroup? = null
    private var loadingView: View? = null
    private var tab: TabLayout? = null
    private var tabPager: ViewPager2? = null
    private var studyId: Int? = null
    private var studyDetailId: Int? = null
    private val studyViewModel: StudyViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private val noticeViewModel: NoticeViewModel by viewModels()
    private val courseStudyFragmentViewModel: CourseStudyFragmentViewModel by activityViewModels()
    private lateinit var courseStudy: CourseStudy
    private lateinit var rule: Rule
    private lateinit var faceCheckActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var preSavedStateHandle: SavedStateHandle
    private var needOnceCheck = false
    private var currentNeedLivenessCheck: Boolean = true
    private var faceChecking: Boolean = false
    private var facePageBright: Boolean = false
    private lateinit var videoPlaySetting: VideoPlay

    //??????????????????????????????????????????????????????
    private var studyState: StudyStateEnum = StudyStateEnum.NOT_STARTED
    private var studyProcess: Double = 0.0
    private lateinit var dispatcher: OnBackPressedDispatcher
    private val mTimeUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val action = intent.action
            if (action == null || action.isEmpty()) return
            if (action == Intent.ACTION_TIME_CHANGED) {
                //????????????????????????????????????
                onSystemTimeChange()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        faceCheckActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    data?.apply {
                        var stage =
                            getSerializableExtra("stage") as FaceCheckStrategy.Stage
                        violationCheckStrategy?.cancel()
                        changeStateText(null)
                        cancelFaceCheckState()
                        stage = when (stage) {
                            FaceCheckStrategy.Stage.BEFORE -> {
                                FaceCheckStrategy.Stage.STUDYING
                            }
                            FaceCheckStrategy.Stage.STUDYING -> {
                                stage
                            }
                            FaceCheckStrategy.Stage.AFTER -> {
                                onStudyFinished()
                                stage
                            }
                            else -> {
                                FaceCheckStrategy.Stage.STUDYING
                            }
                        }
                        if (!needOnceCheck && stage != FaceCheckStrategy.Stage.AFTER) {
                            faceCheckStrategy?.startFaceCheckDelay(stage)
                        }
                    }
                } else if (it.resultCode == Activity.RESULT_CANCELED) {
                    faceChecking = false
                }
            }
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (isFirstLoad) {
            initView()
            isFirstLoad = false
        }
        return rootView
    }

    private fun initView() {
        binding = FragmentCourseStudyBinding.inflate(layoutInflater)
        rootView = binding!!.root
        player = binding!!.player
        panelMain = binding!!.panelMain
        loadingView = binding!!.detailsLoading
        tab = binding!!.tab
        tabPager = binding!!.tabPager
        subjectId = arguments?.getInt("subjectId")
        classId = arguments?.getInt("classId")
        coursePosition = arguments?.getInt("coursePosition")
        courseId = arguments?.getInt("courseId")
        courseName = arguments?.getString("courseName")
        videoPoster = arguments?.getString("subjectImageUrl")
        ViewCompat.setTransitionName(rootView!!, "study")
        //postponeEnterTransition()
        initPlayerPoster()
        initCourse(SCREEN_NORMAL)
        val currentBackStackEntry = findNavController().currentBackStackEntry!!
        preSavedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<QuizStateEnum>("quiz").observe(currentBackStackEntry) {
            resetQuizState(it)
        }
        savedStateHandle.getLiveData<String>("setting").observe(currentBackStackEntry) {
            when (it) {
                "player" -> {
                    updatePlayerSetting()
                }
            }
        }
        dispatcher = requireActivity().onBackPressedDispatcher
        var onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (player.screen == Jzvd.SCREEN_FULLSCREEN) {
                    Jzvd.backPress()
                    return
                } else {
                    this.isEnabled = false
                    if (currentPlayedSeconds > 0 || courseStudy.courseStudyInfo.StudiedSeconds == 0L) {
                        if (currentPlayedSeconds > 0) {
                            saveStudyByLocal(currentPlayedSeconds)
                            saveStudyTodayByLocal(currentPlayedSeconds)
                            saveStudyByRemote(currentPlayedSeconds) {}
                        }
                        preSavedStateHandle[STUDY_STATE] =
                            CourseStudyBack(coursePosition!!, courseId!!, studyState, studyProcess)
                    }
                    dispatcher.onBackPressed()
                }
            }
        }
        dispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private fun initCourse(screenModel: Int) {
        changeStateText("?????????????????????...")
        //UI??????????????????
        settingViewModel.getSetting().observe(viewLifecycleOwner) {
            facePageBright = if (!it.hasFacePageBright()) {
                true
            } else {
                it.facePageBright
            }
            videoPlaySetting = it.videoPlay
            //????????????
            studyViewModel.getRuleFromWeb().observe(viewLifecycleOwner) { rule ->
                this.rule = rule
                changeStateText("????????????????????????...")
                //????????????????????????
                studyViewModel.getCourseStudyFromLocal(
                    subjectId!!,
                    courseId!!,
                    rule.StudyRule.MaxAllowStudyCourseHoursEveryDay != null
                ).observe(viewLifecycleOwner) { localData ->
                    //???????????????
                    studyViewModel.initCourse(
                        subjectId!!,
                        courseId!!,
                        localData.courseStudyData?.studiedSeconds,
                        localData.courseStudyDayData?.studiedSeconds,
                        localData.courseStudyData?.updateTime?.time
                    ).observe(viewLifecycleOwner) { courseInitResult ->
                        studyViewModel.clearCourseStudyFromLocal(
                            localData.courseStudyData,
                            localData.courseStudyDayData
                        ).observe(viewLifecycleOwner) {
                            studyId = courseInitResult.CourseStudyId
                            studyDetailId = courseInitResult.CourseStudyDetailId
                            changeStateText("???????????????????????????...")
                            //????????????????????????????????????????????????????????????????????????????????????
                            studyViewModel.getCourseStudy(
                                subjectId!!,
                                courseId!!,
                                courseInitResult.CourseStudyId
                            ).observe(viewLifecycleOwner) { courseStudy ->
                                this.courseStudy = courseStudy
                                //???????????????????????????????????????
                                studyViewModel.needOnceFaceCheck(
                                    subjectId!!,
                                    courseId!!,
                                    rule.FaceCheckRule.FaceCheckTimeRule
                                ).observe(viewLifecycleOwner) { needOnceCheck ->
                                    this.needOnceCheck = needOnceCheck
                                    changeStateText("?????????????????????...")
                                    //??????????????????
                                    initPlayer(screenModel) {
                                        //?????????????????????
                                        initStudyState(courseInitResult.IsStarted)
                                        //viewModel?????????????????????
                                        initProgressForViewModel()
                                        //viewModel?????????????????????
                                        initStudyStateForViewModel(courseInitResult.IsStarted)
                                        //viewModel???????????????????????????
                                        initQuizStateForViewModel()
                                        //viewModel?????????????????????
                                        initStudyStepForViewModel()
                                        //tabView?????????
                                        initTabView()
                                        //???????????????????????????????????????????????????????????????
                                        preInspection { preCheckIsPass ->
                                            if (preCheckIsPass) {
                                                changeStateText("???????????????????????????...")
                                                //???????????????????????????
                                                initFaceCheckStrategy(needOnceCheck)
                                                if (!courseStudy.courseStudyInfo.IsFinished) {
                                                    studyProcess =
                                                        courseStudy.courseStudyInfo.Progress
                                                    changeStateText("?????????????????????...")
                                                    //?????????????????????
                                                    initViolationCheckStrategy()
                                                }
                                            }
                                            changeStateText(null)
                                            loadingView?.visibility = GONE
                                            panelMain?.visibility = VISIBLE
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initTabView() {
        val fragments =
            mutableListOf(
                CourseInfoFragment.newInstance(
                    this.courseStudy.courseInfo,
                    this.courseStudy.courseStudyInfo,
                    this.rule
                ),
                QuizFragment.newInstance(
                    this.courseStudy.courseInfo,
                    this.courseStudy.courseStudyInfo,
                    this.rule
                )
            )
        tabPager!!.adapter = StudyFragmentStateAdapter(
            this.childFragmentManager,
            lifecycle,
            fragments
        )
        tabPager!!.isSaveEnabled = false
        TabLayoutMediator(
            tab!!,
            tabPager!!
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "??????"
                }
                1 -> {
                    tab.text = "????????????"
                }
            }
        }.attach()
    }

    private fun initPlayerPoster() {
        Glide.with(requireContext()).load(this.videoPoster)
            .into(player.posterImageView)
    }

    private fun initPlayer(
        screenModel: Int,
        initializationCompleted: () -> Unit
    ) {
        var alertAtNotWifi: Boolean = if (!videoPlaySetting.hasAlertAtNotWifi()) {
            true
        } else {
            videoPlaySetting.alertAtNotWifi
        }
        var gesture: Boolean = if (!videoPlaySetting.hasGesture()) {
            true
        } else {
            videoPlaySetting.gesture
        }
        player.isBannedPlay = false
        player.isAlertAtNotWifi = alertAtNotWifi
        player.isGesture = gesture
        if (this.courseStudy.courseStudyInfo.IsFinished) {
            player.initStudySeconds = 0L
            player.timeProgressBarRule = VideoPlayer.TimeProgressBarRule.NORMAL
            player.setUp(
                this.courseStudy.courseInfo.VideoUrl,
                this.courseStudy.courseInfo.CourseName,
                screenModel,
                JZMediaSystem::class.java
            )
        } else {
            player.initStudySeconds = this.courseStudy.courseStudyInfo.StudiedSeconds
            currentPlayedSeconds = player.initStudySeconds
            if (currentPlayedSeconds != courseStudyFragmentViewModel.studiedSeconds.value) {
                courseStudyFragmentViewModel.studiedSeconds.value = currentPlayedSeconds
            }
            player.timeProgressBarRule = VideoPlayer.TimeProgressBarRule.SEEK_BAN
            if (player.jzDataSource == null) {
                player.setUp(
                    this.courseStudy.courseInfo.VideoUrl,
                    this.courseStudy.courseInfo.CourseName,
                    screenModel,
                    JZMediaAliyun::class.java
                )
            }
        }
        player.setStartVideoListener {
            if (it) {
                faceCheckStrategy?.currentStage?.let {
                    toFaceCheck(faceCheckStrategy?.currentStage!!, currentNeedLivenessCheck)
                }
            }
        }
        player.setVideoPlayingListener {
            if (it) {
                faceCheckStrategy?.currentStage?.let {
                    toFaceCheck(faceCheckStrategy?.currentStage!!, currentNeedLivenessCheck)
                }
            }
        }
        player.setScreenStateChangeListener { isFullScreen ->
            activity?.let {
                if (isFullScreen) {
//                    immersionBar {
//                        hideBar(BarHide.FLAG_HIDE_BAR)
//                    }
                } else {
//                    immersionBar {
//                        statusBarColor(R.color.black)
//                        statusBarDarkFont(false)
//                        hideBar(BarHide.FLAG_SHOW_BAR)
//                    }
                }
            }
        }
        player.setOnReplayListener { isRestudy ->
            if (isRestudy) {
                initCourse(player.screen)
            }
        }
        player.setStudyTimeChangeListener { milliseconds ->
            if (!this.courseStudy.courseStudyInfo.IsFinished) {
                var seconds = floor(milliseconds / 1000.0)
                if (seconds.toLong() > 0 && seconds.toLong() != currentPlayedSeconds) {
                    currentPlayedSeconds = seconds.toLong()
                    courseStudyFragmentViewModel.studiedSeconds.value = currentPlayedSeconds
                    courseHourLimitStrategy?.check(currentPlayedSeconds)
                    classTimeLimitStrategy?.checkClassEndTime()
                    //?????????????????????
                    if (currentPlayedSeconds % localDBSaveLimitSeconds == 0L) {
                        saveStudyByLocal(currentPlayedSeconds)
                        saveStudyTodayByLocal(currentPlayedSeconds)
                    }
                    //?????????????????????
                    if (currentPlayedSeconds % remoteDBSaveLimitSeconds == 0L) {
                        saveStudyByRemote(currentPlayedSeconds) { needOnceCheck ->
                            if (needOnceCheck) {
                                faceCheckStrategy?.startFaceCheck(FaceCheckStrategy.Stage.STUDYING)
                            }
                        }
                    }
                    setProgress(seconds * 100 / this.courseStudy.courseInfo.VideoSeconds)
                }
            }
        }
        //????????????
        player.setCompletionListener {
            if (!this.courseStudy.courseStudyInfo.IsFinished) {
                this.courseStudy.courseStudyInfo.IsFinished = true
                faceCheckStrategy?.cancelTimer()
                saveStudyByRemote(this.courseStudy.courseInfo.VideoSeconds) {
                    if (this.rule.FaceCheckRule.NeedCheck && this.rule.FaceCheckRule.NeedCheckAtFinished) {
                        courseStudyFragmentViewModel.studiedSeconds.value =
                            this.courseStudy.courseInfo.VideoSeconds
                        courseStudyFragmentViewModel.studyProgress.value = 100
                        courseStudyFragmentViewModel.studyStep.value =
                            StudyStepEnum.CHECKING_AT_FINISHED
                        faceCheckStrategy?.startFaceCheck(FaceCheckStrategy.Stage.AFTER)
                    } else {
                        onStudyFinished()
                    }
                }
            }
        }
        player.setOnBackClickListener {
            if (!it) {
                dispatcher.onBackPressed()
            }
        }
        player.setOnMoreButtonClickListener {
            val modalBottomSheet = PlayerDialogFragment.newInstance()
            modalBottomSheet.setOnSettingClickListener {
                if (player.screen == SCREEN_FULLSCREEN) {
                    Jzvd.backPress()
                }
                findNavController()?.navigate(
                    R.id.action_courseStudyFragment_to_videoPlayFragment,
                    null,
                    null,
                    null
                )
            }
            modalBottomSheet.setOnNoticeClickListener {
                if (player.screen == SCREEN_FULLSCREEN) {
                    Jzvd.backPress()
                }
                findNavController().navigate(
                    NavGraphDirections.actionGlobalWebFragment(
                        "????????????",
                        "${BuildConfig.REMOTE_DOMAIN}/Content/Notice.html",
                        null
                    )
                )
            }
            modalBottomSheet.show(
                requireActivity().supportFragmentManager,
                PlayerDialogFragment.TAG
            )
        }
        initializationCompleted()
    }

    private fun updatePlayerSetting() {
        settingViewModel.getSetting().observe(this) {
            facePageBright = if (!it.hasFacePageBright()) {
                true
            } else {
                it.facePageBright
            }
            videoPlaySetting = it.videoPlay
            var alertAtNotWifi: Boolean = if (!videoPlaySetting.hasAlertAtNotWifi()) {
                true
            } else {
                videoPlaySetting.alertAtNotWifi
            }
            var gesture: Boolean = if (!videoPlaySetting.hasGesture()) {
                true
            } else {
                videoPlaySetting.gesture
            }
            player.isAlertAtNotWifi = alertAtNotWifi
            player.isGesture = gesture
        }
    }

    private fun initProgressForViewModel() {
        if (this.courseStudy.courseStudyInfo.IsFinished) {
            setProgress(100.0)
        } else {
            setProgress(this.courseStudy.courseStudyInfo.Progress)
        }
    }

    private fun setProgress(progress: Double) {
        var progressInt = round(progress).toInt()
        studyProcess = progressInt.toDouble()
        courseStudyFragmentViewModel.studyProgress.value = progressInt
    }

    private fun initStudyState(isStarted: Boolean) {
        studyState = if (isStarted) StudyStateEnum.STUDYING else StudyStateEnum.NOT_STARTED
        if (this.courseStudy.courseStudyInfo.IsFinished) {
            var quizPass: Boolean = if (this.rule.QuizRule.NeedQuiz) {
                when (EnumUtils.getQuizStateEnum(this.courseStudy.courseStudyInfo.QuizState)) {
                    QuizStateEnum.PASS -> {
                        true
                    }
                    else -> {
                        false
                    }
                }

            } else {
                true
            }
            var lastFaceCheckFinished: Boolean =
                if (this.rule.FaceCheckRule.NeedCheck && this.rule.FaceCheckRule.NeedCheckAtFinished) {
                    this.courseStudy.courseStudyInfo.LastFaceCheckFinished
                } else {
                    true
                }
            if (quizPass && lastFaceCheckFinished) {
                studyState = StudyStateEnum.FINISHED
            }
        }
    }


    private fun initStudyStateForViewModel(isStarted: Boolean) {
        var studyStateForViewModel =
            if (isStarted) StudyStateEnum.STUDYING else StudyStateEnum.NOT_STARTED
        if (this.courseStudy.courseStudyInfo.IsFinished) {
            var lastFaceCheckFinished: Boolean =
                if (this.rule.FaceCheckRule.NeedCheck && this.rule.FaceCheckRule.NeedCheckAtFinished) {
                    this.courseStudy.courseStudyInfo.LastFaceCheckFinished
                } else {
                    true
                }
            if (lastFaceCheckFinished) {
                studyStateForViewModel = StudyStateEnum.FINISHED
            }
        }
        courseStudyFragmentViewModel.studyState.value = studyStateForViewModel
    }

    private fun initQuizStateForViewModel() {
        //?????????null????????????????????????
        courseStudyFragmentViewModel.quizState.value = null
    }

    private fun initStudyStepForViewModel() {
        if (this.courseStudy.courseStudyInfo.IsFinished) {
            if (this.rule.FaceCheckRule.NeedCheck && this.rule.FaceCheckRule.NeedCheckAtFinished) {
                if (this.courseStudy.courseStudyInfo.LastFaceCheckFinished) {
                    if (this.rule.QuizRule.NeedQuiz) {
                        if (this.courseStudy.courseStudyInfo.QuizState == QuizStateEnum.PASS.value) {
                            courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.FINISHED
                        } else {
                            courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.QUIZZING
                        }
                    } else {
                        courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.FINISHED
                    }
                } else {
                    courseStudyFragmentViewModel.studyStep.value =
                        StudyStepEnum.CHECKING_AT_FINISHED
                }
            } else {
                if (this.rule.QuizRule.NeedQuiz) {
                    if (this.courseStudy.courseStudyInfo.QuizState == QuizStateEnum.PASS.value) {
                        courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.FINISHED
                    } else {
                        courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.QUIZZING
                    }
                } else {
                    courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.FINISHED
                }
            }
        } else {
            courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.STUDYING
        }
    }

    private fun resetQuizState(quizState: QuizStateEnum) {
        when (quizState) {
            QuizStateEnum.PASS -> {
                this.courseStudy.courseStudyInfo.QuizState = QuizStateEnum.PASS.value
                preSavedStateHandle[STUDY_STATE] =
                    CourseStudyBack(coursePosition!!, courseId!!, StudyStateEnum.FINISHED, 100.0)
                studyState = StudyStateEnum.FINISHED
                courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.FINISHED
            }
            QuizStateEnum.NOT_PASS -> {
                this.courseStudy.courseStudyInfo.QuizState = QuizStateEnum.STUDY_RESET.value
            }
            QuizStateEnum.STUDY_RESET -> {
                studyViewModel.saveStudiedSeconds(subjectId!!, courseId!!, 0)
                    .observe(this) {
                        studyProcess = 0.0
                        Snackbar.make(rootView!!, "??????????????????????????????????????????????????????", Snackbar.LENGTH_LONG)
                            .show()
                        resetViewForRestudy()
                    }
            }
            else -> {

            }
        }
        courseStudyFragmentViewModel.quizState.value = quizState
    }

    private fun onStudyFinished() {
        courseStudyFragmentViewModel.studiedSeconds.value = this.courseStudy.courseInfo.VideoSeconds
        courseStudyFragmentViewModel.studyProgress.value = 100
        if (this.rule.QuizRule.NeedQuiz) {
            courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.QUIZZING
            courseStudyFragmentViewModel.studyState.value = StudyStateEnum.FINISHED
        } else {
            courseStudyFragmentViewModel.studyStep.value = StudyStepEnum.FINISHED
            studyState = StudyStateEnum.FINISHED
        }
    }

    private fun saveStudyByLocal(studiedSeconds: Long) {
        studyViewModel.saveStudiedSeconds(subjectId!!, courseId!!, studiedSeconds)
            .observe(viewLifecycleOwner) {

            }
    }

    private fun saveStudyTodayByLocal(studiedSeconds: Long) {
        this.rule.StudyRule.MaxAllowStudyCourseHoursEveryDay?.let {
            var studiedSecondsToday =
                getStudiedSecondsToday(studiedSeconds)
            studyViewModel.saveTodayStudiedSeconds(subjectId!!, courseId!!, studiedSecondsToday)
                .observe(viewLifecycleOwner) {

                }
        }
    }

    private fun saveStudyByRemote(studiedSeconds: Long, callback: (Boolean) -> Unit) {
        var studiedSecondsToday =
            getStudiedSecondsToday(studiedSeconds)
        studyViewModel.saveStudy(
            courseStudy.courseInfo.SubjectId,
            courseStudy.courseInfo.ProfessionId,
            courseStudy.courseInfo.GradeId,
            courseStudy.courseInfo.CourseId,
            studiedSeconds,
            studiedSecondsToday,
            studyDetailId!!
        )
            .observe(viewLifecycleOwner) {
                callback(it)
            }
    }

    private fun getStudiedSecondsToday(studiedSecondsTotal: Long): Long {
        return studiedSecondsTotal - courseStudy.courseStudyInfo.StudiedSeconds + courseStudy.courseStudyInfo.StudiedSecondsToday
    }

    private fun initFaceCheckStrategy(
        needOnceCheck: Boolean,
    ) {
        faceCheckStrategy = FaceCheckStrategy(
            this.rule,
            this.courseStudy.courseInfo,
            this.courseStudy.courseStudyInfo,
            needOnceCheck
        )
        faceCheckStrategy?.setFaceCheckListener { stage, needLivenessCheck ->
            toFaceCheck(stage, needLivenessCheck)
        }
        faceCheckStrategy?.init();
    }

    private fun initViolationCheckStrategy() {
        violationCheckStrategy =
            ViolationCheckStrategy(
                this.rule.FaceCheckRule.MaxAllowCheckSeconds,
                this.rule.FaceCheckRule.MaxAllowViolatedNumber,
                courseStudy.courseStudyInfo.ViolateNumber
            )
        val faceCheckOffsetTime = FaceCheckOffsetTime()
        faceCheckOffsetTime.violationCheckStrategyHashCode = violationCheckStrategy.hashCode()
        faceCheckOffsetTime.maxAllowCheckSeconds = this.rule.FaceCheckRule.MaxAllowCheckSeconds
        faceCheckOffsetTime.maxAllowViolatedNumber = this.rule.FaceCheckRule.MaxAllowViolatedNumber
        (activity?.application as MyApplication).faceCheckOffsetTime =
            faceCheckOffsetTime
        violationCheckStrategy?.setViolatedListener { violateNumber, isMax ->
            if (isMax) {
                resetStudy()
            } else {
                updateViolateCount(violateNumber)
            }
        }

    }

    private fun initCourseHourLimitStrategy(onFirstCheckedCallback: (Boolean) -> Unit) {
        if (courseStudy.courseStudyInfo.IsFinished) {
            onFirstCheckedCallback(true)
        } else {
            studyViewModel.getNextDayMilliseconds()
                .observe(viewLifecycleOwner) { nextDayMilliseconds ->
                    courseHourLimitStrategy = CourseHourLimitStrategy(
                        this.rule.StudyRule.MaxAllowStudyCourseHoursEveryDay,
                        this.rule.StudyRule.MinutesEveryCourseHours,
                        this.courseStudy.courseStudyInfo.TotalStudiedSecondsToday,
                        this.courseStudy.courseStudyInfo.StudiedSeconds,
                        nextDayMilliseconds
                    )
                    courseHourLimitStrategy?.setOnLimitCourseHourListener {
                        setLimitCourseHourState()
                        saveStudyByLocal(it)
                        saveStudyTodayByLocal(it)
                    }
                    courseHourLimitStrategy?.setOnFirstCourseHourLimitCheckedListener { isPass ->
                        if (!isPass) {
                            setLimitCourseHourState()
                        }
                        onFirstCheckedCallback(isPass)
                    }
                    courseHourLimitStrategy?.setOnDaySpanListener {
                        this.view?.post {
                            this.courseStudy.courseStudyInfo.StudiedSeconds = currentPlayedSeconds
                            this.courseStudy.courseStudyInfo.TotalStudiedSecondsToday = 0
                            this.courseStudy.courseStudyInfo.StudiedSecondsToday = 0
                            saveStudyByRemote(currentPlayedSeconds) {
                                courseHourLimitStrategy?.cancelTimer()
                                courseHourLimitStrategy = null
                                initCourseHourLimitStrategy {

                                }
                            }
                        }
                    }
                    courseHourLimitStrategy?.init()
                }
        }
    }

    private fun initExamStateLimitStrategy(onCheckedCallback: (Boolean) -> Unit) {
        onCheckedCallback(!this.courseStudy.courseStudyInfo.IsExamAllowed || this.courseStudy.courseStudyInfo.IsFinished)
    }

    private fun preInspection(callback: (Boolean) -> Unit) {
        //??????????????????
        checkLocalTime { isLocalTimeCheckPass ->
            if (isLocalTimeCheckPass) {
                //????????????????????????????????????????????????????????????
                initClassTimeLimitStrategy { isClassTimePass ->
                    if (isClassTimePass) {
                        //?????????????????????????????????
                        initCourseHourLimitStrategy { isCourseHourPass ->
                            if (isCourseHourPass) {
                                //?????????????????????????????????
                                initExamStateLimitStrategy { isExamStatePass ->
                                    if (!isExamStatePass) {
                                        setStudyBannedState("???????????????????????????????????????????????????")
                                    }
                                    callback(isExamStatePass)
                                }
                            } else {
                                callback(false)
                            }
                        }
                    } else {
                        callback(false)
                    }
                }
            } else {
                callback(false)
            }
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        registerReceiver(requireContext(), mTimeUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    private fun checkLocalTime(callback: (Boolean) -> Unit) {
        studyViewModel.getTimeStamp().observe(this) {
            if (!it.isLocalTime) {
                setStudyBannedState("?????????????????????????????????????????????????????????????????????")
                callback(false)
            } else {
                callback(true)
            }
        }
    }

    private fun initClassTimeLimitStrategy(onFirstCheckedCallback: (Boolean) -> Unit) {
        if (classId != null) {
            studyViewModel.getClassRuleAndTimeStamp(classId!!).observe(viewLifecycleOwner) {
                val classRule = it.first
                val timeStampInfo = it.second
                classRule?.let {
                    classTimeLimitStrategy =
                        ClassTimeLimitStrategy(classRule, timeStampInfo)
                    classTimeLimitStrategy?.setOnClassTimeLimitListener { message ->
                        setStudyBannedState(message)
                    }
                    classTimeLimitStrategy?.setOnFirstClassTimeLimitCheckedListener { isPass ->
                        onFirstCheckedCallback(isPass)
                    }
                    classTimeLimitStrategy?.init()
                }
            }
        } else {
            onFirstCheckedCallback(true)
        }
    }

    private fun updateViolateCount(violateNumber: Int) {
        this.view?.post {
            studyViewModel.updateViolation(
                studyId!!,
                subjectId!!,
                courseId!!,
                violateNumber
            )
                .observe(viewLifecycleOwner) {
                    if (player.isBannedPlay) {
                        changeStateText("?????????${violateNumber}?????????????????????????????????")
                    }
                }
        }
    }

    private fun resetStudy() {
        this.view?.post {
            violationCheckStrategy?.cancel()
            studyViewModel.saveStudiedSeconds(subjectId!!, courseId!!, 0)
                .observe(viewLifecycleOwner) {
                    studyViewModel.resetStudy(
                        studyId!!,
                        subjectId!!,
                        courseId!!
                    )
                        .observe(viewLifecycleOwner) {
                            currentPlayedSeconds = 0
                            courseStudyFragmentViewModel.studiedSeconds.value = currentPlayedSeconds
                            var notification =
                                NotificationCompat.Builder(requireContext(), "resetStudy")
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentTitle("????????????????????????????????????")
                                    .setStyle(
                                        NotificationCompat.BigTextStyle()
                                            .bigText("?????????????????????????????????????????????????????????????????????????????????????????????????????????${this.courseStudy.courseInfo.CourseName}????????????????????????????????????????????????")
                                    )
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                            with(NotificationManagerCompat.from(requireContext())) {
                                notify(1, notification.build())
                            }
                            changeStateText("???????????????????????????????????????????????????")
                            resetViewForRestudy()
                        }
                }
        }
    }

    private fun toFaceCheck(stage: FaceCheckStrategy.Stage, needLivenessCheck: Boolean) {
        if (!faceChecking) {
            this.view?.post {
                setFaceCheckState()
                violationCheckStrategy?.startCheck()
                currentNeedLivenessCheck = needLivenessCheck
                val faceCheckOffsetTime =
                    (activity?.application as MyApplication).faceCheckOffsetTime
                faceCheckOffsetTime?.startCheckTimeStamp = Date().time
                faceCheckOffsetTime?.violateNumber = violationCheckStrategy?.violateNumber
                faceCheckOffsetTime?.currentCountDownMilliseconds =
                    violationCheckStrategy?.currentCountDownMilliseconds
                faceCheckActivityLauncher.launch(
                    Intent(
                        context,
                        FaceAuthActivity::class.java
                    )
                        .putExtra("studyId", studyId)
                        .putExtra("courseStudy", courseStudy)
                        .putExtra("stage", stage)
                        .putExtra("facePageBright", facePageBright)
                        .putExtra("needLivenessCheck", needLivenessCheck)
                        .putExtra(
                            "violationCheckStrategyHashCode",
                            violationCheckStrategy.hashCode()
                        )
                        .putExtra(
                            "maxAllowCheckSeconds",
                            violationCheckStrategy?.maxAllowCheckSeconds
                        )
                        .putExtra(
                            "maxAllowCheckSeconds",
                            violationCheckStrategy?.maxAllowCheckSeconds
                        )
                        .putExtra(
                            "maxAllowViolatedNumber",
                            violationCheckStrategy?.maxAllowViolatedNumber
                        )
                        .putExtra("violateNumber", violationCheckStrategy?.violateNumber)
                        .putExtra(
                            "violateCountDownMilliseconds",
                            violationCheckStrategy?.currentCountDownMilliseconds
                        )
                )
            }
        }
    }

    private fun setFaceCheckState() {
        this.view?.post {
            faceChecking = true
            player.isBannedPlay = true
            player.startButtonState.isPreFaceCheck = true
            if (player.getState() == Jzvd.STATE_PLAYING) {
                player.pause()
            }
            player.setFaceCheckState()
            changeStateText("?????????????????????")
        }
    }

    private fun setLimitCourseHourState() {
        setStudyBannedState("?????????????????????????????????????????????????????????")
    }

    private fun setStudyBannedState(state: String) {
        this.view?.post {
            player.isBannedPlay = true
            player.pause()
            changeStateText(state)
            resetViewForBanStudy()
        }
    }

    private fun cancelFaceCheckState() {
        this.view?.post {
            faceChecking = false
            player.isBannedPlay = false
            player.startButtonState.isPreFaceCheck = false
            changeStateText(null)
            if (!this.courseStudy.courseStudyInfo.IsFinished) {
                player.play()
                player.cancelFaceCheckState()
            }
        }
    }

    private fun changeStateText(state: String?) {
        player.message = state
    }

    private fun onSystemTimeChange() {
        checkLocalTime {
            if (!it) {
                player.isBannedPlay = true
                player.pause()
            } else {
                changeStateText(null)
                player.isBannedPlay = false
            }
        }
    }

    private fun resetViewForRestudy() {
        classTimeLimitStrategy?.let {
            it.cancelTimer()
        }
        courseHourLimitStrategy?.let {
            it.cancelTimer()
        }
        faceCheckStrategy?.let {
            it.cancelTimer()
        }
        violationCheckStrategy?.let {
            it.cancel()
        }
        classTimeLimitStrategy = null
        courseHourLimitStrategy = null
        faceCheckStrategy = null
        violationCheckStrategy = null
        player.resetViewForRestudy()
    }

    private fun resetViewForBanStudy() {
        classTimeLimitStrategy?.let {
            it.cancelTimer()
        }
        courseHourLimitStrategy?.let {
            it.cancelTimer()
        }
        faceCheckStrategy?.let {
            it.cancelTimer()
        }
        violationCheckStrategy?.let {
            it.cancel()
        }
        classTimeLimitStrategy = null
        courseHourLimitStrategy = null
        faceCheckStrategy = null
        violationCheckStrategy = null
        player.resetViewForBanStudy()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CourseStudyFragment()

        const val STUDY_STATE: String = "STUDY_STATE"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Jzvd.releaseAllVideos()
        binding = null
        if (faceCheckStrategy != null) {
            faceCheckStrategy?.cancelTimer()
        }
    }

    override fun onResume() {
        if (!player.isBannedPlay && !player.isRestudy) {
            Jzvd.goOnPlayOnResume()
        }
        super.onResume()
    }

    override fun onPause() {
        player?.let {
            if (player.getState() == Jzvd.STATE_PLAYING) {
                player.pause()
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        classTimeLimitStrategy?.let {
            it.cancelTimer()
        }
        courseHourLimitStrategy?.let {
            it.cancelTimer()
        }
        faceCheckStrategy?.let {
            it.cancelTimer()
        }
        violationCheckStrategy?.let {
            it.cancel()
        }
        Jzvd.releaseAllVideos()
        (activity?.application as MyApplication).faceCheckOffsetTime = null
        super.onDestroy()
    }

}