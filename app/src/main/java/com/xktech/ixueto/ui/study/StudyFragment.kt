package com.xktech.ixueto.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialElevationScale
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.StudyFragmentStateAdapter
import com.xktech.ixueto.databinding.FragmentStudyBinding
import com.xktech.ixueto.di.NetworkModule
import com.xktech.ixueto.ui.login.LoginFragment
import com.xktech.ixueto.viewModel.StudyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudyFragment : Fragment() {
    private var binding: FragmentStudyBinding? = null
    private lateinit var navController: NavController
    private lateinit var tab: TabLayout
    private lateinit var pager: ViewPager2
    private lateinit var loginPanel: ViewGroup
    private lateinit var goLoginButton: Button
    private lateinit var mainPanel: ViewGroup
    private var isFirstLoad = true
    private var rootView: View? = null
    private val studyViewModel: StudyViewModel by viewModels()
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
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(isFirstLoad) {
            isFirstLoad = false
            navController = findNavController()
            val currentBackStackEntry = navController.currentBackStackEntry!!
            currentBackStackEntry.savedStateHandle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESSFUL)
                .observe(currentBackStackEntry) {
                    if (it) {
                        initStudyView()
                    }
                }
            initStudyView()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            StudyFragment()
    }

    private fun initView() {
        binding = FragmentStudyBinding.inflate(layoutInflater)
        rootView = binding!!.root
        pager = binding!!.pager
        pager.isSaveEnabled = false
        tab = binding!!.tab
        loginPanel = binding!!.loginPanel
        mainPanel = binding!!.mainPanel
        goLoginButton = binding!!.goLogin
        goLoginButton.setOnClickListener {
            navController.navigate(R.id.action_global_loginFragment)
        }
    }

    private fun initStudyView() {
        if (NetworkModule.token.isNullOrEmpty()) {
            pager.adapter = StudyFragmentStateAdapter(
                this.childFragmentManager,
                lifecycle,
                mutableListOf()
            )
            loginPanel.visibility = View.VISIBLE
            mainPanel.visibility = View.GONE
        } else {
            mainPanel.visibility = View.VISIBLE
            loginPanel.visibility = View.GONE
            studyViewModel.getRuleFromWeb().observe(this) {
                studyViewModel.setRule(it).observe(this) {
                    val fragments =
                        mutableListOf(
                            ClassesStudyFragment.newInstance(),
                            PersonalStudyFragment.newInstance()
                        )
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
                            0 -> {
                                tab.text = "班次学习"
                                //tab.icon =  ContextCompat.getDrawable(context!!, R.drawable.ic_class)
                            }
                            1 -> {
                                tab.text = "个人学习"
                                //tab.icon =  ContextCompat.getDrawable(context!!, R.drawable.ic_study)
                            }
                        }
                    }.attach()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}