package com.xktech.ixueto.ui.quiz

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.transition.MaterialContainerTransform
import com.xktech.ixueto.R
import com.xktech.ixueto.components.TilingDrawable
import com.xktech.ixueto.databinding.FragmentQuizResultQuestionBinding
import com.xktech.ixueto.model.QuizQuestionAnswer
import com.xktech.ixueto.viewModel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import net.nightwhistler.htmlspanner.HtmlSpanner

@AndroidEntryPoint
class QuizResultQuestionFragment : Fragment() {
    private var isFirstLoad = true
    private var rootView: View? = null
    private var binding: FragmentQuizResultQuestionBinding? = null
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var questionId: Int? = null
    private var courseName: String? = null
    private var questionIds: MutableList<Int>? = null
    private var currentIndex: Int = 0
    private lateinit var toolBar: MaterialToolbar
    private lateinit var questionTypeView: TextView
    private lateinit var questionContentView: TextView
    private lateinit var questionOptionView: TextView
    private lateinit var currentOrderView: TextView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var questionPrev: MenuItem
    private lateinit var questionNext: MenuItem
    private lateinit var questionContainer: LinearLayout
    private lateinit var resultView: TextView
    private lateinit var answerView: TextView
    private lateinit var answerReference: TextView
    private lateinit var explainsView: TextView
    private lateinit var wavyLineView: View
    private val quizViewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.question_prev -> {
                    if (currentIndex > 0) {
                        currentIndex--
                        loadQuestionAnswer()
                        true
                    }
                    false
                }
                R.id.question_next -> {
                    if (currentIndex < questionIds!!.size - 1) {
                        currentIndex++
                        loadQuestionAnswer()
                        true
                    }
                    false
                }
                R.id.answer_sheet -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun initView() {
        binding = FragmentQuizResultQuestionBinding.inflate(layoutInflater)
        rootView = binding!!.root
        subjectId = arguments?.getInt("subjectId")
        courseId = arguments?.getInt("courseId")
        questionId = arguments?.getInt("questionId")
        courseName = arguments?.getString("courseName")
        toolBar = binding!!.toolBar
        questionTypeView = binding!!.questionType
        questionContentView = binding!!.questionContent
        questionOptionView = binding!!.questionOptions
        currentOrderView = binding!!.questionOrder
        bottomAppBar = binding!!.bottomAppBar
        questionContainer = binding!!.questionContainer
        resultView = binding!!.result
        answerView = binding!!.answer
        answerReference = binding!!.answerReference
        explainsView = binding!!.explains
        wavyLineView = binding!!.wavyLine
        toolBar.subtitle = courseName
        var menu = bottomAppBar.menu
        questionPrev = menu[0]
        questionNext = menu[1]
        wavyLineView.background = TilingDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_wavy_line)!!)
        ViewCompat.setTransitionName(rootView!!, "question_detail")
        initQuiz()
    }

    private fun initQuiz() {
        questionContainer.loadSkeleton()
        quizViewModel.getQuestions(subjectId!!, courseId!!).observe(viewLifecycleOwner) {
            questionIds = it
            currentIndex = it.indexOf(questionId)
            if (currentIndex == -1) {
                currentIndex = 0
            }
            if (it.size > 1) {
                if (currentIndex == 0) {
                    questionPrev.isEnabled = true
                }
                if (currentIndex < it.size - 1) {
                    questionNext.isEnabled = true
                }
            }
            loadQuestionAnswer(true)
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
                questionAnswerRendering(it){
                    questionContainer.hideSkeleton()
                }
            }
        }
    }

    private fun getQuestionAnswer(callback: ((QuizQuestionAnswer) -> Unit)? = null) {
        var questionId = questionIds?.get(currentIndex)
        if (questionId != null) {
            quizViewModel.getQuestionAnswer(subjectId!!, courseId!!, questionId)
                .observe(viewLifecycleOwner) { quizQuestionAnswer ->
                    callback?.let {
                        it?.let {
                            callback(quizQuestionAnswer)
                        }
                    }
                }
        }
    }

    private fun questionAnswerRendering(
        quizQuestionAnswer: QuizQuestionAnswer,
        callback: (() -> Unit)? = null
    ) {
        questionTypeView.text = "【${quizQuestionAnswer.Type}】"
        questionContentView.text = HtmlSpanner().fromHtml(quizQuestionAnswer.Question)
        questionOptionView.text = HtmlSpanner().fromHtml(quizQuestionAnswer.Options)
        currentOrderView.text = "第${(currentIndex + 1)}/${questionIds?.size}题"
        questionPrev.isEnabled = currentIndex != 0
        questionNext.isEnabled = currentIndex != questionIds?.size?.minus(1) ?: 0
        if (quizQuestionAnswer.IsRight == null) {
            resultView.text = "未作答"
        } else {
            if (quizQuestionAnswer.IsRight == true) {
                if (context?.resources?.configuration!!.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    resultView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.success_dark
                        )
                    )
                } else {
                    resultView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.success
                        )
                    )
                }
                resultView.text = "正确"

            } else {
                if (context?.resources?.configuration!!.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    resultView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.error_dark
                        )
                    )
                } else {
                    resultView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.error
                        )
                    )
                }
                resultView.text = "错误"
            }
        }
        answerView.text = quizQuestionAnswer.Answer
        answerReference.text = quizQuestionAnswer.AnswerReference
        explainsView.text = HtmlSpanner().fromHtml(quizQuestionAnswer.Explains)
        if (callback != null) {
            callback()
        }
    }

    private fun playFadeOutAnimation(view: View, callback: () -> Unit) {
        val fadeOutAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0.3f).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }
        fadeOutAnimator.start()
        fadeOutAnimator.addListener(object: Animator.AnimatorListener {
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
        val fadeOutAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }
        fadeOutAnimator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuizResultQuestionFragment()
    }
}