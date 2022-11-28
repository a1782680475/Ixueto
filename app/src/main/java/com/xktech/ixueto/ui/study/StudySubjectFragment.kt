package com.xktech.ixueto.ui.study

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.StudyFragmentStateAdapter
import com.xktech.ixueto.databinding.FragmentStudySubjectBinding
import com.xktech.ixueto.ui.exam.ExamFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class StudySubjectFragment : Fragment() {
    private var binding: FragmentStudySubjectBinding? = null
    private lateinit var toolBar: Toolbar
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var subjectImageView: ImageView
    private lateinit var professionNameView: TextView
    private lateinit var subjectNameView: TextView
    private var isFirstLoad = true
    private var rootView: View? = null
    private var subjectId: Int? = null
    private lateinit var tab: TabLayout
    private lateinit var pager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (isFirstLoad) {
            isFirstLoad = false
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            StudySubjectFragment()
    }

    private fun initView() {
        binding = FragmentStudySubjectBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        appBarLayout = binding!!.top
        collapsingToolbarLayout = binding!!.collapsingToolbarLayout
        pager = binding!!.pager
        pager.isSaveEnabled = false
        tab = binding!!.tab
        subjectImageView = binding!!.subjectImage
        professionNameView = binding!!.professionName
        subjectNameView = binding!!.subjectName
        subjectId = arguments?.getInt("subjectId")
        val professionId = arguments?.getInt("professionId")
        val gradeId = arguments?.getInt("gradeId")
        ViewCompat.setTransitionName(rootView!!, "studySubject")
        val professionText = arguments?.getString("professionName")
        val subjectImageUrlText = arguments?.getString("subjectImage")
        val subjectText = arguments?.getString("subjectName")
        var classId = arguments?.getInt("classId", 0) ?: 0
        Glide.with(requireContext()).load(subjectImageUrlText).into(subjectImageView)
        professionNameView.text = professionText
        subjectNameView.text = subjectText
        val fragments = mutableListOf<Fragment>(
            StudyCourseFragment.newInstance(subjectId!!, classId, subjectImageUrlText!!),
            StudySubjectDetailsFragment.newInstance(subjectId!!),
            ExamFragment.newInstance(subjectId!!, professionId!!, gradeId!!)
        )
        pager.offscreenPageLimit = 1
        pager.adapter = StudyFragmentStateAdapter(
            this.childFragmentManager,
            lifecycle,
            fragments
        )
        TabLayoutMediator(
            tab,
            pager
        ) { tab, position ->
            when (position) {
                0 -> tab.text = "课程"
                1 -> tab.text = "详情"
                2 -> tab.text = "考试"
            }
        }.attach()
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
                collapsingToolbarLayout.title = "学习详情"
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}