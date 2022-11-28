package com.xktech.ixueto.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentQuizBinding
import com.xktech.ixueto.model.*
import com.xktech.ixueto.utils.EnumUtils
import com.xktech.ixueto.viewModel.CourseStudyFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class QuizFragment : Fragment() {
    private var binding: FragmentQuizBinding? = null
    private var rootView: View? = null
    private lateinit var quizNoneContainer: LinearLayout
    private lateinit var quizWaitContainer: LinearLayout
    private lateinit var quizGoContainer: LinearLayout
    private lateinit var quizShowContainer: LinearLayout
    private lateinit var quizStartButton: Button
    private lateinit var quizShowButton: Button
    private lateinit var courseInfo: CourseInfo
    private lateinit var studyInfo: CourseStudyInfo
    private lateinit var rule: Rule
    private var studyState: StudyStateEnum = StudyStateEnum.STUDYING
    private var quizViewState: QuizViewStateEnum = QuizViewStateEnum.NONE
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
        binding = FragmentQuizBinding.inflate(layoutInflater)
        rootView = binding!!.root
        quizNoneContainer = binding!!.quizNoneContainer
        quizWaitContainer = binding!!.quizWaitContainer
        quizGoContainer = binding!!.quizGoContainer
        quizShowContainer = binding!!.quizShowContainer
        quizShowButton = binding!!.quizShow
        quizStartButton = binding!!.quizStart
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
        courseStudyFragmentViewModel.studyState.observe(viewLifecycleOwner) {
            initQuiz()
        }
        courseStudyFragmentViewModel.quizState.observe(viewLifecycleOwner){
            it?.let {
                resetQuizState(it)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(courseInfo: CourseInfo, studyInfo: CourseStudyInfo, rule: Rule) =
            QuizFragment().apply {
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

    private fun initQuiz() {
        if (rule.QuizRule.NeedQuiz) {
            if (courseStudyFragmentViewModel.studyState.value == StudyStateEnum.FINISHED) {
                initQuizState(EnumUtils.getQuizStateEnum(studyInfo.QuizState))
            } else {
                quizViewState = QuizViewStateEnum.WAIT
            }
        } else {
            quizViewState = QuizViewStateEnum.NONE
        }
        resetQuizView()
    }

    private fun resetQuizView() {
        when (quizViewState) {
            QuizViewStateEnum.NONE -> {
                quizNoneContainer.visibility = View.VISIBLE
                quizWaitContainer.visibility = View.GONE
                quizGoContainer.visibility = View.GONE
                quizShowContainer.visibility = View.GONE
            }
            QuizViewStateEnum.WAIT -> {
                quizNoneContainer.visibility = View.GONE
                quizWaitContainer.visibility = View.VISIBLE
                quizGoContainer.visibility = View.GONE
                quizShowContainer.visibility = View.GONE
            }
            QuizViewStateEnum.NOT_PASS -> {
                quizNoneContainer.visibility = View.GONE
                quizWaitContainer.visibility = View.GONE
                quizGoContainer.visibility = View.VISIBLE
                quizShowContainer.visibility = View.GONE
            }
            QuizViewStateEnum.PASS -> {
                quizNoneContainer.visibility = View.GONE
                quizWaitContainer.visibility = View.GONE
                quizGoContainer.visibility = View.GONE
                quizShowContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun initQuizState(quizState: QuizStateEnum) {
        when (quizState) {
            QuizStateEnum.NOT_QUIZ,
            QuizStateEnum.NOT_PASS -> {
                quizViewState = QuizViewStateEnum.NOT_PASS
                quizStartButton.setOnClickListener {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                    var bundle = bundleOf(
                        "professionId" to courseInfo.ProfessionId,
                        "subjectId" to courseInfo.SubjectId,
                        "courseId" to courseInfo.CourseId,
                        "courseName" to courseInfo.CourseName,
                    )
                    parentFragment?.findNavController()?.navigate(
                        R.id.action_courseStudyFragment_to_quizRuleFragment,
                        bundle,
                        null,
                        null
                    )
                }
            }
            QuizStateEnum.PASS -> {
                quizViewState = QuizViewStateEnum.PASS
                quizShowButton.setOnClickListener {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                    var bundle = bundleOf(
                        "professionId" to courseInfo.ProfessionId,
                        "subjectId" to courseInfo.SubjectId,
                        "courseId" to courseInfo.CourseId,
                        "courseName" to courseInfo.CourseName,
                    )
                    parentFragment?.findNavController()?.navigate(
                        R.id.action_courseStudyFragment_to_quizResultFragment,
                        bundle,
                        null,
                        null
                    )
                }
                studyState = StudyStateEnum.FINISHED
            }
            else -> {

            }
        }
    }

    private fun resetQuizState(quizState: QuizStateEnum) {
        when (quizState) {
            QuizStateEnum.PASS -> {
                quizViewState = QuizViewStateEnum.PASS
                studyState = StudyStateEnum.FINISHED
            }
            QuizStateEnum.NOT_PASS -> {
                quizViewState = QuizViewStateEnum.NOT_PASS
            }
            QuizStateEnum.STUDY_RESET -> {
                quizViewState = QuizViewStateEnum.WAIT
            }
            else->{

            }
        }
        resetQuizView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private enum class QuizViewStateEnum() : Serializable {
        NONE,
        WAIT,
        PASS,
        NOT_PASS,
    }
}