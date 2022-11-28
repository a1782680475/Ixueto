package com.xktech.ixueto.ui.subject

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.SubjectDetailsCourseAdapter
import com.xktech.ixueto.components.LCEERecyclerView
import com.xktech.ixueto.databinding.FragmentSubjectDetailsBinding
import com.xktech.ixueto.viewModel.CourseViewModel
import com.xktech.ixueto.viewModel.SubjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class SubjectDetailsFragment : Fragment() {
    private var binding: FragmentSubjectDetailsBinding? = null
    private lateinit var toolBar: Toolbar
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var recyclerView: LCEERecyclerView
    private lateinit var courseAdapter: SubjectDetailsCourseAdapter
    private lateinit var subjectImage: ImageView
    private lateinit var professionName: TextView
    private lateinit var subjectName: TextView
    private lateinit var courseNumber: TextView
    private lateinit var courseHours: TextView
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var signUp: Button
    private lateinit var savedStateHandle: SavedStateHandle
    private var isFirstLoad = true
    private var rootView: View? = null
    private val courseViewModel: CourseViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()
    private var subjectId: Int? = null
    private var examineState: Short? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isFirstLoad) {
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        savedStateHandle = navController.previousBackStackEntry!!.savedStateHandle
        savedStateHandle[SIGNUP] = Pair(subjectId!!, examineState!!)
        if (isFirstLoad) {
            isFirstLoad = false
            loadData()
            super.onViewCreated(view, savedInstanceState)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SubjectDetailsFragment()

        const val SIGNUP: String = "SIGNUP"
    }


    private fun initView() {
        binding = FragmentSubjectDetailsBinding.inflate(layoutInflater)
        rootView = binding!!.root
        appBarLayout = binding!!.top
        toolBar = binding!!.toolBar
        recyclerView = binding!!.courseRecycler
        subjectImage = binding!!.subjectImage
        professionName = binding!!.professionName
        subjectName = binding!!.subjectName
        courseNumber = binding!!.courseNumber
        courseHours = binding!!.courseHours
        collapsingToolbarLayout = binding!!.collapsingToolbarLayout
        signUp = binding!!.signUp
        subjectId = arguments?.getInt("subjectId")
        signUp.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle("报名确认")
                    .setMessage("确定报名该专题？")
                    .setNegativeButton(resources.getString(R.string.cancel), null)
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        subjectViewModel.signUp(subjectId!!).observe(this) {
                            if (it.result) {
                                updateSignUpState(0)
                                savedStateHandle[SIGNUP] = Pair(subjectId!!, 0)
                                Snackbar.make(rootView!!, it.message, Snackbar.LENGTH_SHORT)
                                    .show()
                            } else {
                                Snackbar.make(rootView!!, it.message, Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                    .show()
            }
        }
        ViewCompat.setTransitionName(rootView!!, "subject")
        var toolbarTitleColorId = R.color.md_theme_light_onSurface
        if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            toolbarTitleColorId = R.color.md_theme_dark_onSurface
        }
        val toolbarTitleColor = ContextCompat.getColor(requireContext(), toolbarTitleColorId)
        val toolbarTitleColorRedHex = Integer.toHexString(toolbarTitleColor.red)
        val toolbarTitleColorGreenHex = Integer.toHexString(toolbarTitleColor.green)
        val toolbarTitleColorBlueHex = Integer.toHexString(toolbarTitleColor.blue)
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout: AppBarLayout, i: Int ->
            val rage = abs(i).toDouble() / appBarLayout.totalScrollRange
            if (rage > 0.8) {
                collapsingToolbarLayout.title = "专题详情"
                with(toolBar) { setNavigationIcon(R.drawable.ic_baseline_arrow_back) }
                var alphaHex =
                    Integer.toHexString((rage * toolbarTitleColor.alpha).toInt())
                if (alphaHex.length == 1) {
                    alphaHex = "0${alphaHex}"
                }
                var color =
                    Color.parseColor("#${alphaHex}${toolbarTitleColorRedHex}${toolbarTitleColorGreenHex}${toolbarTitleColorBlueHex}")
                collapsingToolbarLayout.setCollapsedTitleTextColor(color)
            } else {
                collapsingToolbarLayout.title = ""
                if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    with(toolBar) { setNavigationIcon(R.drawable.selector_arrow_back_dark) }
                } else {
                    with(toolBar) { setNavigationIcon(R.drawable.selector_arrow_back) }
                }
            }
        })
    }


    private fun loadData() {
        recyclerView.showLoadingView()
        val layoutManager = LinearLayoutManager(context)
        courseAdapter = SubjectDetailsCourseAdapter(requireContext()) { course, viewHolder ->

        }
        recyclerView.recyclerView.layoutManager = layoutManager
        recyclerView.recyclerView.adapter = courseAdapter
        val subjectImageUrlText = arguments?.getString("subjectImage")
        val professionNameText = arguments?.getString("professionName")
        val subjectNameText = arguments?.getString("subjectName")
        val courseNumberText = arguments?.getInt("courseNumber")
        val courseHoursText = arguments?.getInt("courseHours")
        examineState = arguments?.getShort("examineState")
        savedStateHandle[SIGNUP] = Pair(subjectId!!, examineState)
        updateSignUpState(examineState)
        Glide.with(requireContext()).load(subjectImageUrlText).into(subjectImage)
        professionName.text = professionNameText
        subjectName.text = subjectNameText
        courseNumber.text = "${courseNumberText.toString()}章节"
        courseHours.text = "${courseHoursText.toString()}课时"
        lifecycleScope.launch {
            subjectId?.let {
                courseViewModel.getCourses(it).collectLatest { pagingData ->
                    courseAdapter.submitData(pagingData)
                }
            }
        }
        courseAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.Loading) {
                recyclerView.showLoadingView()
            }
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && courseAdapter.itemCount == 0) {
                recyclerView.showEmptyView()
            } else {
                recyclerView.hideAllViews()
            }
        }
    }

    private fun updateSignUpState(examineState: Short?) {
        var signUpIsEnabled = true
        var signUpButtonText = "立即报名"
        when (examineState?.toInt()) {
            0 -> {
                signUpIsEnabled = false
                signUpButtonText = "审核中"
            }
            1 -> {
                signUpIsEnabled = false
                signUpButtonText = "已报名"
            }
            2 -> {
                signUpIsEnabled = false
                signUpButtonText = "审核未通过"
            }
        }
        signUp.isEnabled = signUpIsEnabled
        signUp.text = signUpButtonText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}