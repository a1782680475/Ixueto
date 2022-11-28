package com.xktech.ixueto.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.xktech.ixueto.databinding.FragmentCourseInfoBinding
import com.xktech.ixueto.model.CourseInfo
import com.xktech.ixueto.model.CourseStudyInfo
import com.xktech.ixueto.model.Rule
import com.xktech.ixueto.model.StudyStepEnum
import com.xktech.ixueto.viewModel.CourseStudyFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import params.com.stepview.StatusViewScroller
import kotlin.math.round

@AndroidEntryPoint
class CourseInfoFragment : Fragment() {
    private lateinit var courseInfo: CourseInfo
    private lateinit var studyInfo: CourseStudyInfo
    private lateinit var rule: Rule
    private lateinit var courseTitle: TextView
    private lateinit var statusView: StatusViewScroller
    private lateinit var currentStudiedSecondsView: TextView
    private lateinit var totalSecondsView: TextView
    private lateinit var progressTextView: TextView
    private lateinit var stateCardViewGroup: ViewGroup
    private lateinit var progressBar: LinearProgressIndicator
    private var binding: FragmentCourseInfoBinding? = null
    private var rootView: View? = null
    private var currentStudiedSeconds = 0L
    private var isViewChange = false
    private val courseStudyFragmentViewModel: CourseStudyFragmentViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseInfo = it.getSerializable(PARAM_COURSE_INFO) as CourseInfo
            studyInfo = it.getSerializable(PARAM_STUDY_INFO) as CourseStudyInfo
            rule = it.getSerializable(PARAM_RULE) as Rule
        }
    }

    private fun initView() {
        binding = FragmentCourseInfoBinding.inflate(layoutInflater)
        rootView = binding!!.root
        courseTitle = binding!!.courseTitle
        statusView = binding!!.statusView
        currentStudiedSecondsView = binding!!.currentStudiedSeconds
        totalSecondsView = binding!!.totalSeconds
        progressTextView = binding!!.progressText
        progressBar = binding!!.progressBar
        stateCardViewGroup = binding!!.stateCard
//        val dm: DisplayMetrics = requireContext().resources.displayMetrics
//        val whScale = dm.widthPixels.toDouble() / dm.heightPixels
//        isViewChange = isChangeView(whScale)
//        if(isViewChange){
//            changeView()
//        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        courseTitle.text = courseInfo.CourseName
        initStatusView()
        initSecondsView()
        initProgressView()
    }

    companion object {
        @JvmStatic
        fun newInstance(courseInfo: CourseInfo, studyInfo: CourseStudyInfo, rule: Rule) =
            CourseInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(PARAM_COURSE_INFO, courseInfo)
                    putSerializable(PARAM_STUDY_INFO, studyInfo)
                    putSerializable(PARAM_RULE, rule)
                }
            }

        private const val PARAM_COURSE_INFO = "courseInfo"
        private const val PARAM_STUDY_INFO = "studyInfo"
        private const val PARAM_RULE = "rule"
    }

    private fun isChangeView(whScale: Double): Boolean {
        return whScale > 0.6f
    }

    private fun changeView(){
//        val lp = (stateCardViewGroup.layoutParams as LinearLayout.LayoutParams).apply {
//            weight = 0.65f
//        }
//        stateCardViewGroup.layoutParams = lp

    }

    private fun initStatusView() {
        var stepList = mutableListOf("学习")
        if (this.rule.FaceCheckRule.NeedCheck && this.rule.FaceCheckRule.NeedCheckAtFinished) {
            stepList.add("尾部识别")
        }
        if (this.rule.QuizRule.NeedQuiz) {
            stepList.add("单元测试")
        }
        statusView.statusView.run {
            stepCount = stepList.size
            setStatusList(stepList)
            lineColor =
                currentCount
        }
        courseStudyFragmentViewModel.studyStep.observe(viewLifecycleOwner) {
            var currentCount1 = 1
            it?.let {
                currentCount1 = when (it) {
                    StudyStepEnum.NOT_STARTED -> {
                        0
                    }
                    StudyStepEnum.STUDYING -> {
                        1
                    }
                    StudyStepEnum.CHECKING_AT_FINISHED -> {
                        2
                    }
                    StudyStepEnum.QUIZZING -> {
                        statusView.statusView.stepCount
                    }
                    StudyStepEnum.FINISHED -> {
                        statusView.statusView.stepCount + 1
                    }
                }
            }
            statusView.statusView.run {
                currentCount = currentCount1
            }
        }
    }

    private fun initSecondsView() {
        totalSecondsView.text = "/${this.courseInfo.VideoSeconds}秒"
        if (studyInfo.IsFinished) {
            currentStudiedSecondsView.text = this.courseInfo.VideoSeconds.toString()
        }
        courseStudyFragmentViewModel.studiedSeconds.observe(viewLifecycleOwner) {
            if (!studyInfo.IsFinished && it != currentStudiedSeconds) {
                currentStudiedSeconds = it
                currentStudiedSecondsView.text = it.toString()
            }
        }
    }

    private fun initProgressView() {
        if (this.studyInfo.IsFinished) {
            setProgressView(100.0)
        } else {
            courseStudyFragmentViewModel.studyProgress.value = this.studyInfo.Progress.toInt()
            courseStudyFragmentViewModel.studyProgress.observe(viewLifecycleOwner) {
                it?.let {
                    setProgressView(it.toDouble())
                }
            }
        }
    }

    private fun setProgressView(progress: Double) {
        var progressInt = round(progress).toInt()
        if (progressInt > 100) {
            progressInt = 100
        }
        if(!isViewChange) {
            progressTextView?.text = "${progressInt}%"
            progressBar?.progress = progressInt
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}