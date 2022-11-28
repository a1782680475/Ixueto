package com.xktech.ixueto.ui.subject

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
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.SubjectAdapter
import com.xktech.ixueto.components.LCEERecyclerView
import com.xktech.ixueto.databinding.FragmentSubjectBinding
import com.xktech.ixueto.viewModel.SubjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs


@AndroidEntryPoint
class SubjectFragment : Fragment() {
    private var binding: FragmentSubjectBinding? = null
    private var isFirstLoad = true
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolBar: Toolbar
    private lateinit var professionImage: ImageView
    private lateinit var professionName: TextView
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var subjectRecycler: LCEERecyclerView
    private lateinit var subject: View
    private val subjectModel: SubjectViewModel by viewModels()
    private var professionId: Int = -1
    private var rootView: View? = null
    private lateinit var navController: NavController
    private var signUpMap: MutableMap<Int, Short> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            scrimColor = Color.TRANSPARENT
            isElevationShadowEnabled = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
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
        if (isFirstLoad) {
            isFirstLoad = false
            bindData()
        }
    }

    private fun initView() {
        binding = FragmentSubjectBinding.inflate(layoutInflater)
        rootView = binding!!.root
        appBarLayout = binding!!.top
        toolBar = binding!!.toolBar
        collapsingToolbarLayout = binding!!.collapsingToolbarLayout
        professionImage = binding!!.professionImage
        professionName = binding!!.professionName
        subjectRecycler = binding!!.subjectRecycler
        subject = binding!!.subject
        navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Pair<Int, Short>>(SubjectDetailsFragment.SIGNUP)
            .observe(currentBackStackEntry, Observer { signUpResult ->
                signUpMap[signUpResult.first] = signUpResult.second
            })
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
                collapsingToolbarLayout.title = "专题"
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

    private fun bindData() {
        professionId = arguments?.getInt("professionId")!!
        val professionNameText = arguments?.getString("professionName")
        val professionImageUrlText = arguments?.getString("professionImage")
        Glide.with(requireContext()).load(professionImageUrlText).into(professionImage)
        professionName.text = professionNameText
        subjectRecycler.recyclerView.layoutManager = LinearLayoutManager(context)
        subjectRecycler.showLoadingView()
        subjectModel.getSubjects(professionId).observe(viewLifecycleOwner) {
            val subjectAdapter = SubjectAdapter(context, it) { subject, viewHolder ->
                val extras = FragmentNavigatorExtras(viewHolder.card to "subject")
                var bundle = bundleOf("subjectId" to subject.Id)
                bundle.putString("subjectImage", subject.CoverImageUrl)
                bundle.putString("professionName", professionNameText)
                bundle.putString("subjectName", subject.Name)
                bundle.putInt("courseNumber", subject.CourseNumber)
                bundle.putInt("courseHours", subject.CourseHours)
                var examineState: Short = -1
                examineState = if (signUpMap.containsKey(subject.Id)) {
                    signUpMap[subject.Id]!!
                } else {
                    subject.ExamineState ?: -1
                }
                bundle.putShort("examineState", examineState)
                parentFragment?.findNavController()?.navigate(
                    R.id.action_subjectFragment_to_subjectDetailsFragment,
                    bundle,
                    null,
                    extras
                )
            }
            subjectRecycler.recyclerView.adapter = subjectAdapter
            if (it.isNotEmpty()) {
                subjectRecycler.hideAllViews()

            } else {
                subjectRecycler.showEmptyView()
            }
        }
        ViewCompat.setTransitionName(rootView!!, "profession")
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            SubjectFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}