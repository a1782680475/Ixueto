package com.xktech.ixueto.ui.quiz

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialElevationScale
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.QuizQuestionAnswerBriefAdapter
import com.xktech.ixueto.components.LCEERecyclerView
import com.xktech.ixueto.databinding.FragmentQuizResultBinding
import com.xktech.ixueto.model.QuizStateEnum
import com.xktech.ixueto.viewModel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuizResultFragment : Fragment() {
    private var isFirstLoad = true
    private var rootView: View? = null
    private var binding: FragmentQuizResultBinding? = null
    private var professionId: Int? = null
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var courseName: String? = null
    private lateinit var resultView: TextView
    private lateinit var questionRecycler: LCEERecyclerView
    private lateinit var toolBar: MaterialToolbar
    private lateinit var totalNumberView: TextView
    private lateinit var answeredNumberView: TextView
    private lateinit var rightNumberView: TextView
    private lateinit var retakeButton: Button
    private val quizViewModel: QuizViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
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
        binding = FragmentQuizResultBinding.inflate(layoutInflater)
        rootView = binding!!.root
        professionId = arguments?.getInt("professionId")
        subjectId = arguments?.getInt("subjectId")
        courseId = arguments?.getInt("courseId")
        courseName = arguments?.getString("courseName")
        toolBar = binding!!.toolBar
        questionRecycler = binding!!.questionRecycler
        totalNumberView = binding!!.totalNumber
        answeredNumberView = binding!!.answeredNumber
        rightNumberView = binding!!.rightNumber
        resultView = binding!!.result
        retakeButton = binding!!.retake
        toolBar.subtitle = courseName
        loadData()
        val currentBackStackEntry = findNavController().currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        val preSavedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle.getLiveData<QuizStateEnum>("quiz").observe(currentBackStackEntry){
            it.let {
                preSavedStateHandle["quiz"] = it
            }
        }
    }

    private fun loadData() {
        quizViewModel.getQuizResult(subjectId!!, courseId!!).observe(viewLifecycleOwner) {
            if (it.IsPass) {
                if (context?.resources?.configuration!!.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    resultView.setTextColor(
                        ContextCompat.getColor(requireContext(),
                        R.color.answer_right_dark))
                } else {
                    resultView.setTextColor(
                        ContextCompat.getColor(requireContext(),
                        R.color.answer_right))
                }
                resultView.text = "测试通过"

            } else {
                if (context?.resources?.configuration!!.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    resultView.setTextColor(
                        ContextCompat.getColor(requireContext(),
                        R.color.answer_error_dark))
                } else {
                    resultView.setTextColor(
                        ContextCompat.getColor(requireContext(),
                        R.color.answer_error))
                }
                resultView.text = "测试未通过"
            }
            totalNumberView.text = it.QuestionNumber.toString()
            answeredNumberView.text = it.AnswerNumber.toString()
            rightNumberView.text = it.RightAnswerNumber.toString()
            if(!it.IsPass){
                retakeButton.visibility = View.VISIBLE
                retakeButton.setOnClickListener {
                    var bundle = bundleOf(
                        "professionId" to professionId,
                        "subjectId" to subjectId,
                        "courseId" to courseId,
                        "courseName" to courseName
                    )
                    parentFragment?.findNavController()?.navigate(
                        R.id.action_quizResultFragment_to_quizRuleFragment,
                        bundle,
                        null,
                        null
                    )
                }
            }
        }
        questionRecycler.recyclerView.layoutManager = LinearLayoutManager(context)
        questionRecycler.showLoadingView()
        val questionAdapter = QuizQuestionAnswerBriefAdapter(requireContext()) { question, viewHolder ->
            val extras = FragmentNavigatorExtras(viewHolder.root to "question_detail")
            var bundle = bundleOf("subjectId" to subjectId,
                "courseId" to courseId,
                "courseName" to courseName,
                "questionId" to question.Id
            )
            parentFragment?.findNavController()?.navigate(
                R.id.action_quizResultFragment_to_quizResultQuestionFragment,
                bundle,
                null,
                extras
            )
        }
        lifecycleScope.launch {
            subjectId?.let {
                quizViewModel.getPageQuestionAnswerBrief(subjectId!!, courseId!!).collectLatest {
                    questionAdapter.submitData(it)
                }
            }
        }
        questionAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.Loading) {
                questionRecycler.showLoadingView()
            }
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && questionAdapter.itemCount == 0) {
                questionRecycler.showEmptyView()
            } else {
                questionRecycler.hideAllViews()
            }
        }
        questionRecycler.recyclerView.adapter = questionAdapter
        //questionRecycler.recyclerView.addItemDecoration(RecycleViewDivider(requireContext(),LinearLayoutManager.VERTICAL))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuizResultFragment()
    }
}