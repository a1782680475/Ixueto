package com.xktech.ixueto.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.StudyCourseAdapter
import com.xktech.ixueto.components.DropDownView
import com.xktech.ixueto.components.LCEERecyclerView
import com.xktech.ixueto.databinding.FragmentStudyCourseBinding
import com.xktech.ixueto.datastore.Rule
import com.xktech.ixueto.model.CourseStudyBack
import com.xktech.ixueto.model.CourseStudyState
import com.xktech.ixueto.viewModel.StudyCourseViewModel
import com.xktech.ixueto.viewModel.StudyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StudyCourseFragment : Fragment() {
    private var binding: FragmentStudyCourseBinding? = null
    private lateinit var recyclerView: LCEERecyclerView
    private var courseAdapter: StudyCourseAdapter? = null
    private lateinit var courseNumberText: TextView
    private var courseNumber: Int = 0
    private lateinit var filter: DropDownView
    private var isFirstLoad = true
    private var rootView: View? = null
    private var rule: Rule? = null
    private var subjectId: Int = 0
    private var classId: Int = 0
    private var subjectImageUrl: String = ""
    private var courseStudyState: CourseStudyState? = null
    private val studyViewModel: StudyViewModel by viewModels()
    private val studyCourseViewModel: StudyCourseViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectId = it.getInt(PARAM_SUBJECT_ID)
            classId = it.getInt(PARAM_CLASS_ID)
            subjectImageUrl = it.getString(PARAM_SUBJECT_IMAGE_URL).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (isFirstLoad || rootView == null) {
            initView()
            //initData()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isFirstLoad) {
            isFirstLoad = false
            initSpinner()
            initData(0)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(subjectId: Int, classId: Int, subjectImageUrl: String) =
            StudyCourseFragment().apply {
                arguments = Bundle().apply {
                    putInt(PARAM_SUBJECT_ID, subjectId)
                    putInt(PARAM_CLASS_ID, classId)
                    putString(PARAM_SUBJECT_IMAGE_URL, subjectImageUrl)
                }
            }

        private const val PARAM_SUBJECT_ID = "subjectId"
        private const val PARAM_CLASS_ID = "classId"
        private const val PARAM_SUBJECT_IMAGE_URL = "subjectImageUrl"
    }

    private fun initView() {
        binding = FragmentStudyCourseBinding.inflate(layoutInflater)
        rootView = binding!!.root
        navController = findNavController()
        recyclerView = binding!!.courseRecycler
        courseNumberText = binding!!.courseNumber
        filter = binding!!.filter
    }

    private fun initSpinner() {
        filter.setOnSelectedClickListener {
            initData(it)
        }
    }


    private fun initData(type: Int) {
        studyViewModel.getStudyCourseCount(subjectId, type).observe(viewLifecycleOwner) {
            courseNumber = it
        }
        studyViewModel.getRule().observe(viewLifecycleOwner) { rule ->
            this.rule = rule
            studyViewModel.getCourseStudyState(subjectId)
                .observe(viewLifecycleOwner) { courseStudyState ->
                    this.courseStudyState = courseStudyState
                    courseAdapter =
                        StudyCourseAdapter(
                            requireContext(),
                            rule!!,
                            courseStudyState!!
                        ) { course, isAllowEntry, viewHolder ->
                            if (isAllowEntry) {
                                val extras = FragmentNavigatorExtras(viewHolder.root to "study")
                                var bundle = bundleOf(
                                    "subjectId" to subjectId,
                                    "subjectImageUrl" to subjectImageUrl,
                                    "coursePosition" to course.Rank - 1,
                                    "courseId" to course.Id,
                                    "courseName" to course.Name,
                                    "classId" to classId
                                )
                                parentFragment?.findNavController()?.navigate(
                                    R.id.action_studySubjectFragment_to_courseStudyFragment,
                                    bundle,
                                    null,
                                    extras
                                )
                            }
                        }
                    loadData(type)
                }
        }
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<CourseStudyBack>(CourseStudyFragment.STUDY_STATE)
            .observe(currentBackStackEntry) { studyState ->
                studyViewModel.getCourseStudyState(subjectId)
                    .observe(this) { courseStudyState ->
                        courseAdapter?.let {
                            courseAdapter?.updateCourseStudyState(courseStudyState)
                            courseAdapter?.updateCurrentCourseStudyState(studyState)
                            //courseAdapter.notifyDataSetChanged()
                            if (this.rule!!.studyRule.isCourseRegularStudy) {
                                courseAdapter?.notifyItemChanged(studyState.coursePosition - 1)
                                courseAdapter?.notifyItemChanged(studyState.coursePosition)
                                courseAdapter?.notifyItemChanged(studyState.coursePosition + 1)
                            } else {
                                courseAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
            }
    }

    private fun loadData(type: Int) {
        val layoutManager = LinearLayoutManager(context)
        recyclerView.recyclerView.layoutManager = layoutManager
        recyclerView.recyclerView.adapter = courseAdapter
        lifecycleScope.launch {
            subjectId?.let { it ->
                studyCourseViewModel.getStudyCourses(it, type).collectLatest { pagingData ->
                    courseAdapter?.submitData(pagingData)
                }
            }
        }
        courseAdapter?.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.Loading) {
                recyclerView.showLoadingView()
            } else {
                if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && courseAdapter?.itemCount == 0) {
                    recyclerView.showEmptyView()
                    courseNumberText.text = "共0节课"
                } else {
                    recyclerView.hideAllViews()
                    courseNumberText.text = "共${courseNumber}节课"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}