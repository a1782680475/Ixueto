package com.xktech.ixueto.ui.quiz

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentQuizAnswerBinding
import com.xktech.ixueto.model.QuizQuestion
import com.xktech.ixueto.model.QuizStateEnum
import com.xktech.ixueto.utils.DimenUtils
import com.xktech.ixueto.viewModel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import net.nightwhistler.htmlspanner.HtmlSpanner
import java.util.*
import kotlin.math.abs


@AndroidEntryPoint
class QuizAnswerFragment : Fragment() {
    private var isFirstLoad = true
    private var rootView: View? = null
    private var binding: FragmentQuizAnswerBinding? = null
    private var professionId: Int? = null
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var courseName: String? = null
    private var questionIds: List<Int> = mutableListOf()
    private var currentIndex: Int = 0
    private var currentQuizQuestion: QuizQuestion? = null
    private lateinit var toolBar: MaterialToolbar
    private lateinit var questionTypeView: TextView
    private lateinit var questionContentView: TextView
    private lateinit var questionOptionView: TextView
    private lateinit var currentOrderView: TextView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var timerView: TextView
    private lateinit var answerSheetView: MenuItem
    private lateinit var questionPrevView: MenuItem
    private lateinit var questionNextView: MenuItem
    private lateinit var questionContainer: LinearLayout
    private lateinit var answerProgressView: LinearProgressIndicator
    private lateinit var options: ChipGroup
    private lateinit var submit: FloatingActionButton
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var navController: NavController
    private var quizTimer: Timer? = null
    private var quizTimerTask: TimerTask? = null
    private var quizNowTimeStamp: Long = 0
    private var quizEndTimeStamp: Long? = null
    private val quizViewModel: QuizViewModel by viewModels()
    private var checkedIcon: Drawable? = null
    private lateinit var chipProperties: ChipProperty
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding = FragmentQuizAnswerBinding.inflate(layoutInflater)
        rootView = binding!!.root
        professionId = arguments?.getInt("professionId")
        subjectId = arguments?.getInt("subjectId")
        courseId = arguments?.getInt("courseId")
        courseName = arguments?.getString("courseName")
        toolBar = binding!!.toolBar
        questionTypeView = binding!!.questionType
        questionContentView = binding!!.questionContent
        questionOptionView = binding!!.questionOptions
        currentOrderView = binding!!.questionOrder
        bottomAppBar = binding!!.bottomAppBar
        questionContainer = binding!!.questionContainer
        answerProgressView = binding!!.answerProgress
        options = binding!!.options
        submit = binding!!.submit
        timerView = binding!!.timer
        toolBar.subtitle = courseName
        var menu = bottomAppBar.menu
        answerSheetView = menu[0]
        questionPrevView = menu[1]
        questionNextView = menu[2]
        ViewCompat.setTransitionName(rootView!!, "question_detail")
        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        initQuiz()
        val currentBackStackEntry = findNavController().currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        val preSavedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle.getLiveData<QuizStateEnum>("quiz").observe(currentBackStackEntry) {
            it.let {
                preSavedStateHandle["quiz"] = it
                findNavController().popBackStack()
            }
        }
        checkedIcon = ContextCompat.getDrawable(
            requireContext(), com.google.android.material.R.drawable.ic_m3_chip_check
        )
        chipProperties = ChipProperty()
        chipProperties.chipIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle_fill)
        chipProperties.chipTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.md_theme_light_onSurface
        )
        chipProperties.chipTextColorAtChecked = ContextCompat.getColor(
            requireContext(),
            R.color.md_theme_light_primary
        )
        chipProperties.chipIconAtChecked =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle_fill_checked)
        chipProperties.checkedIconColorStateList =
            resources.getColorStateList(R.color.md_theme_light_onPrimary, requireActivity().theme)
        chipProperties.chipStrokeColorStateList =
            resources.getColorStateList(R.color.md_theme_light_outline, requireActivity().theme)
        chipProperties.chipStrokeColorStateListAtChecked =
            resources.getColorStateList(R.color.md_theme_light_primary, requireActivity().theme)
        if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            chipProperties.chipIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle_fill_dark)
            chipProperties.chipIconAtChecked =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle_fill_checked_dark)
            chipProperties.checkedIconColorStateList = resources.getColorStateList(
                R.color.md_theme_dark_onPrimary,
                requireActivity().theme
            )
            chipProperties.chipTextColor = ContextCompat.getColor(
                requireContext(),
                R.color.md_theme_dark_onSurface
            )
            chipProperties.chipTextColorAtChecked = ContextCompat.getColor(
                requireContext(),
                R.color.md_theme_dark_primary
            )
            chipProperties.chipStrokeColorStateList =
                resources.getColorStateList(R.color.md_theme_dark_outline, requireActivity().theme)
            chipProperties.chipStrokeColorStateListAtChecked =
                resources.getColorStateList(R.color.md_theme_dark_primary, requireActivity().theme)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.question_prev -> {
                    saveAnswer()
                    if (currentIndex > 0) {
                        currentIndex--
                        loadQuestionAnswer()
                        true
                    }
                    false
                }
                R.id.question_next -> {
                    saveAnswer()
                    if (currentIndex < questionIds!!.size - 1) {
                        currentIndex++
                        loadQuestionAnswer()
                        true
                    }
                    false
                }
                R.id.answer_sheet -> {
                    val answerSheetDrawer = AnswerSheetDrawerPopupView(requireContext())
                    answerSheetDrawer.setCreatedListener {
                        quizViewModel.getAnswerSheet(subjectId!!, courseId!!)
                            .observe(viewLifecycleOwner) {
                                answerSheetDrawer.currentQuestionIndex = currentIndex
                                answerSheetDrawer.answerSheetData = it
                                answerSheetDrawer.loadData()
                            }
                    }
                    answerSheetDrawer.setOnSelectedListener { position, _ ->
                        answerSheetDrawer?.dismiss()
                        currentIndex = position
                        loadQuestionAnswer()
                    }
                    if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                        XPopup.Builder(context)
                            .popupPosition(PopupPosition.Left)
                            .isDarkTheme(true)
                            .isLightNavigationBar(false)
                            .hasStatusBarShadow(false)
                            .navigationBarColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_dark_surface_navigation
                                )
                            )
                            .asCustom(answerSheetDrawer)
                            .show()

                    } else {
                        XPopup.Builder(context)
                            .popupPosition(PopupPosition.Left)
                            .isDarkTheme(false)
                            .isLightStatusBar(true)
                            .hasStatusBarShadow(false)
                            .navigationBarColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_light_surface_navigation
                                )
                            )
                            .asCustom(answerSheetDrawer)
                            .show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun initQuiz() {
        questionContainer.loadSkeleton()
        quizViewModel.initQuiz(professionId!!, subjectId!!, courseId!!)
            .observe(viewLifecycleOwner) {
                questionIds = it.QuestionIds
                quizNowTimeStamp = it.NowTimeStamp
                quizEndTimeStamp = it.EndTimeStamp
                answerProgressView.max = questionIds.size
                if (currentIndex == -1) {
                    currentIndex = 0
                }
                if (questionIds.size > 1) {
                    if (currentIndex == 0) {
                        questionPrevView.isEnabled = true
                    }
                    if (currentIndex < questionIds.size - 1) {
                        questionNextView.isEnabled = true
                    }
                }
                loadQuestionAnswer(true)
                if (quizEndTimeStamp != null) {
                    if (questionIds.isNotEmpty()) {
                        startTime()
                    }
                } else {
                    timerView.text = "无限制"
                }
            }
        submit.setOnClickListener {
            if (questionIds.isNotEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("交卷确认")
                    .setMessage("交卷后答案不可修改，确认吗？")
                    .setNegativeButton(resources.getString(R.string.cancel), null)
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        submit()
                    }
                    .show()
            }
        }
    }

    private fun submit() {
        quizTimerTask?.cancel()
        quizTimer?.cancel()
        var answerId = options.checkedChipIds
        var answerList = getAnswerById(answerId)
        quizViewModel.submit(
            professionId!!,
            subjectId!!,
            courseId!!,
            currentQuizQuestion?.Id,
            answerList
        ).observe(viewLifecycleOwner) { result ->
            var bundle = bundleOf(
                "professionId" to professionId,
                "subjectId" to subjectId,
                "courseId" to courseId,
                "result" to result,
                "courseName" to courseName
            )
            parentFragment?.findNavController()?.navigate(
                R.id.action_quizAnswerFragment_to_quizBriefResultFragment,
                bundle,
                null,
                null
            )
        }
    }

    private fun startTime() {
        quizTimer = Timer()
        quizTimerTask = QuizTimerTask()
        quizTimer!!.schedule(quizTimerTask, 0, 1000)
    }

    private fun formatDuration(milliseconds: Long): String? {
        val seconds = abs(milliseconds / 1000)
        val positive = String.format(
            "%02d:%02d:%02d",
            seconds / 3600,
            seconds % 3600 / 60,
            seconds % 60
        )
        return if (milliseconds < 0) "00:00:00" else positive
    }

    private fun quizTimerChange() {
        quizEndTimeStamp?.let {
            var remainTime = it - quizNowTimeStamp
            if (remainTime <= 0) {
                this.view?.post {
                    submit()
                }
            }
            var duration = formatDuration(remainTime)
            quizNowTimeStamp += 1000
            this.view?.post {
                timerView.text = duration
            }
        }
    }

    private fun loadQuestionAnswer(isInit: Boolean = false) {
        if (!isInit) {
            playFadeOutAnimation(questionContainer) {
                getQuestionAnswer {
                    questionAnswerRendering(it)
                    playFadeInAnimation(questionContainer)
                }
            }
        } else {
            getQuestionAnswer {
                questionAnswerRendering(it) {
                    questionContainer.hideSkeleton()
                }
            }
        }
    }

    private fun getQuestionAnswer(callback: ((QuizQuestion) -> Unit)? = null) {
        if (questionIds.isNotEmpty()) {
            var questionId = questionIds?.get(currentIndex)
            if (questionId != null) {
                quizViewModel.getQuestion(subjectId!!, courseId!!, questionId)
                    .observe(viewLifecycleOwner) { quizQuestion ->
                        callback?.let {
                            it?.let {
                                callback(quizQuestion)
                            }
                        }
                    }
            }
        } else {
            Snackbar.make(
                rootView!!,
                "未找到相关题目！",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun questionAnswerRendering(
        quizQuestion: QuizQuestion,
        callback: (() -> Unit)? = null
    ) {
        currentQuizQuestion = quizQuestion
        questionTypeView.text = "【${quizQuestion.Type}】"
        questionContentView.text = HtmlSpanner().fromHtml(quizQuestion.Question)
        questionOptionView.text = HtmlSpanner().fromHtml(quizQuestion.Options)
        currentOrderView.text = "第${(currentIndex + 1)}/${questionIds?.size}题"
        answerProgressView.progress = currentIndex + 1
        questionPrevView.isEnabled = currentIndex != 0
        questionNextView.isEnabled =
            currentIndex != questionIds?.size?.minus(1) ?: 0
        //填充选项
        options.removeAllViews()
        var children: MutableList<Pair<Int, String>> = mutableListOf()
        val optionNumber = quizQuestion.OptionNumber
        var answer = quizQuestion.Answer
        when (quizQuestion.Type) {
            "单选题" -> {
                options.isSingleSelection = true
                optionNumber?.let {
                    for (x in 0 until optionNumber!!) {
                        children.add(Pair(x + 65, (x + 65).toChar().toString()))
                    }
                }
            }
            "多选题" -> {
                options.isSingleSelection = false
                optionNumber?.let {
                    for (x in 0 until optionNumber!!) {
                        children.add(Pair(x + 65, (x + 65).toChar().toString()))
                    }
                }
            }
            "判断题" -> {
                options.isSingleSelection = true
                children.add(Pair(1, "正确"))
                children.add(Pair(0, "错误"))
            }
        }
        for (pair in children) {
            val chip = Chip(requireContext())
            chip.id = pair.first
            chip.isCheckable = true
            chip.isChipIconVisible = true
            chip.chipIcon =
                chipProperties.chipIcon
            chip.isCheckedIconVisible = true
            chip.checkedIcon = checkedIcon
            chip.checkedIconTint = chipProperties.checkedIconColorStateList
            chip.layoutParams =
                ChipGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    DimenUtils.dp2px(requireContext(), 65f).toInt()
                )
            chip.text = pair.second
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chip.setTextColor(
                        chipProperties.chipTextColorAtChecked
                    )
                    chip.chipIcon = chipProperties.chipIconAtChecked
                    chip.chipStrokeColor =
                        chipProperties.chipStrokeColorStateListAtChecked
                } else {
                    chip.setTextColor(
                        chipProperties.chipTextColor
                    )
                    chip.chipIcon = chipProperties.chipIcon
                    chip.chipStrokeColor =
                        chipProperties.chipStrokeColorStateList
                }
            }
            if (!answer.isNullOrEmpty()) {
                when (quizQuestion.Type) {
                    "多选题" -> {
                        answer.split(",").forEach { optionAnswer ->
                            if (optionAnswer == pair.second) {
                                chip.isChecked = true
                            }
                        }
                    }
                    else -> {
                        if (answer == pair.second) {
                            chip.isChecked = true
                        }
                    }
                }
            }
            options.addView(chip)
        }
        if (callback != null) {
            callback()
        }
    }

    private fun saveAnswer() {
        currentQuizQuestion?.let {
            var answerId = options.checkedChipIds
            var answerList = getAnswerById(answerId)
            quizViewModel.saveAnswer(
                professionId!!,
                subjectId!!,
                courseId!!,
                currentQuizQuestion!!.Id,
                answerList
            ).observe(viewLifecycleOwner) {

            }
        }
    }

    private fun getAnswerById(idList: MutableList<Int>): MutableList<String> {
        var answerList: MutableList<String> = mutableListOf()
        for (id in idList) {
            var answer = when (id) {
                0 -> {
                    "错误"
                }
                1 -> {
                    "正确"
                }
                else -> {
                    id.toChar().toString()
                }
            }
            answerList.add(answer)
        }
        return answerList
    }

    private fun playFadeOutAnimation(view: View, callback: () -> Unit) {
        val fadeOutAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0.3f).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }
        fadeOutAnimator.start()
        fadeOutAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                callback()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }

        })
    }

    private fun playFadeInAnimation(view: View) {
        val fadeInAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }
        fadeInAnimator.start()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuizAnswerFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        quizTimer = null
        quizTimerTask = null
    }

    inner class QuizTimerTask() :
        TimerTask() {
        override fun run() {
            quizTimerChange()
        }
    }

    inner class ChipProperty {
        var chipIcon: Drawable? = null
        var chipIconAtChecked: Drawable? = null
        var chipTextColor: Int = 0
        var chipTextColorAtChecked: Int = 0
        var checkedIconColorStateList: ColorStateList? = null
        var chipStrokeColorStateList: ColorStateList? = null
        var chipStrokeColorStateListAtChecked: ColorStateList? = null
    }
}
