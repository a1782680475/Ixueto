package com.xktech.ixueto.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialSharedAxis
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentQuizRuleBinding
import com.xktech.ixueto.model.QuizPassRuleEnum
import com.xktech.ixueto.model.QuizStateEnum
import com.xktech.ixueto.utils.EnumUtils
import com.xktech.ixueto.viewModel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton

@AndroidEntryPoint
class QuizRuleFragment : Fragment() {
    private var isFirstLoad = true
    private var rootView: View? = null
    private var binding: FragmentQuizRuleBinding? = null
    private var professionId: Int? = null
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var courseName: String? = null
    private lateinit var rule: LinearLayout
    private lateinit var questionNumberView: TextView
    private lateinit var questionTimeView: TextView
    private lateinit var questionPassRuleView: TextView
    private lateinit var questionRetakeView: TextView
    private lateinit var quizNumberView: TextView
    private lateinit var questionRemarkView: TextView
    private lateinit var toolBar: MaterialToolbar
    private lateinit var quizStartButton: Button
    private val quizViewModel: QuizViewModel by viewModels()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun initView() {
        binding = FragmentQuizRuleBinding.inflate(layoutInflater)
        rootView = binding!!.root
        professionId = arguments?.getInt("professionId")
        subjectId = arguments?.getInt("subjectId")
        courseId = arguments?.getInt("courseId")
        courseName = arguments?.getString("courseName")
        toolBar = binding!!.toolBar
        toolBar.subtitle = courseName
        rule = binding!!.rule
        quizStartButton = binding!!.quizStart
        questionNumberView = binding!!.questionNumber
        questionTimeView = binding!!.questionTime
        questionPassRuleView = binding!!.questionPassRule
        questionRetakeView = binding!!.questionRetake
        quizNumberView = binding!!.quizNumber
        questionRemarkView = binding!!.questionRemark
        getQuizState()
        quizStartButton.setOnClickListener {
            var bundle = bundleOf(
                "professionId" to professionId,
                "subjectId" to subjectId,
                "courseId" to courseId,
                "courseName" to courseName
            )
            parentFragment?.findNavController()?.navigate(
                R.id.action_quizRuleFragment_to_quizAnswerFragment,
                bundle,
                null,
                null
            )
        }
        val currentBackStackEntry = findNavController().currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        val preSavedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle.getLiveData<QuizStateEnum>("quiz").observe(currentBackStackEntry) {
            it.let {
                preSavedStateHandle["quiz"] = it
                resetQuizState(it)
            }
        }
    }

    private fun getQuizState() {
        rule.loadSkeleton()
        quizViewModel.getQuizRuleAndQuizNumber(subjectId!!, courseId!!)
            .observe(this) {
                val quizRule = it.first.QuizRule
                if (quizRule.NeedQuiz) {
                    if (quizRule.QuestionNumber != null) {
                        questionNumberView.text = "试题数量：${quizRule.QuestionNumber}题"
                    } else {
                        questionNumberView.text = "试题数量：动态"
                    }
                    if (quizRule.MaxAllowSeconds != null) {
                        questionTimeView.text = "测试时长：${quizRule.MaxAllowSeconds!! / 60}分钟"
                    } else {
                        questionTimeView.text = "测试时长：无限制"
                    }
                    val quizPassRule = EnumUtils.getQuizPassRule(quizRule.QuizPassRule)
                    questionPassRuleView.text = "通过标准：" + when (quizPassRule) {
                        QuizPassRuleEnum.UNLIMITED -> "无限制"
                        QuizPassRuleEnum.QUALIFIED -> "及格（正确率>=60%）"
                        QuizPassRuleEnum.ALL_CORRECT -> "满分"
                    }
                    if (quizRule.MaxAllowRetake != null) {
                        questionRetakeView.text = "重考标准：不通过时允许${quizRule.MaxAllowRetake}次补考"
                    } else {
                        questionRetakeView.text = "重考标准：不通过时允许无限次补考"
                    }
                    if (!quizRule.ResetStudyIfNotPass) {
                        questionRemarkView.visibility = View.GONE
                    }
                }
                if (it.second == 0) {
                    quizNumberView.text = "本次考试：首次考试"
                } else {
                    quizNumberView.text = "本次考试：第${it.second}次补考"
                }
                if (quizRule.MaxAllowRetake != null) {
                    if (quizRule.MaxAllowRetake!! < it.second) {
                        quizStartButton.isEnabled = false
                        quizStartButton.text = "您已达到最大补考次数"
                    } else {
                        quizStartButton.visibility = View.VISIBLE
                    }
                }else{
                    quizStartButton.visibility = View.VISIBLE
                }
                rule.hideSkeleton()
            }
    }

    private fun resetQuizState(quizState: QuizStateEnum) {
        when (quizState) {
            QuizStateEnum.PASS -> {
                findNavController().popBackStack()
            }
            QuizStateEnum.NOT_PASS -> {
                getQuizState()
            }
            QuizStateEnum.STUDY_RESET -> {
                findNavController().popBackStack()
            }
            else -> {

            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuizRuleFragment()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}