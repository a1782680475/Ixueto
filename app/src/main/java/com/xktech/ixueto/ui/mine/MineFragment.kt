package com.xktech.ixueto.ui.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentMineBinding
import com.xktech.ixueto.model.StudyProgress
import com.xktech.ixueto.ui.login.LoginFragment
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class MineFragment : Fragment() {
    private var binding: FragmentMineBinding? = null
    private lateinit var usernameTextView: TextView
    private lateinit var realNameTextView: TextView
    private lateinit var loginPanel: ViewGroup
    private lateinit var goLoginButton: Button
    private lateinit var mainPanel: ViewGroup
    private lateinit var avatarImageView: ImageView
    private lateinit var authentication: LinearLayout
    private lateinit var settingButton: LinearLayout
    private lateinit var feedbackButton: LinearLayout
    private lateinit var aboutButton: LinearLayout
    private lateinit var navController: NavController
    private lateinit var courseHoursTotal: TextView
    private lateinit var courseHoursFinished: TextView
    private lateinit var studyProgress: TextView
    private val userViewModel: UserViewModel by activityViewModels()
    private var rootView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        initView()
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry!!
        currentBackStackEntry.savedStateHandle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry) {
                if (it) {
                    setUserInfo()
                }
            }
        setUserInfo()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setUserInfo() {
        userViewModel.getUserInfo().observe(this) { userInfo ->
            userInfo?.let {
                if (!userInfo.token.isNullOrEmpty()) {
                    usernameTextView.text = "账号：${userInfo.username}"
                    realNameTextView.text = userInfo.name
                    loginPanel.visibility = View.GONE
                    userViewModel.getStudyProgressByRemote()
                    userViewModel.studyProgress.observe(this){
                        setProgressInfo(it)
                    }
                    mainPanel.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(userInfo.avatar)
                        .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_avatar37)
                        .transition(
                            DrawableTransitionOptions().crossFade()
                        ).into(avatarImageView)
                } else {
                    loginPanel.visibility = View.VISIBLE
                    mainPanel.visibility = View.GONE
                }
            }
        }
    }

    private fun setProgressInfo(progressInfo: StudyProgress?) {
        progressInfo?.let {
            if (it.TotalCourseHours != null) {
                courseHoursTotal.text = it.TotalCourseHours.toString()
            } else {
                courseHoursTotal.text = "-"
            }
            if (it.FinishedCourseHours != null) {
                courseHoursFinished.text = it.FinishedCourseHours.toString()
            } else {
                courseHoursFinished.text = "-"
            }
            if (it.Progress != null) {
                val df = DecimalFormat("######0.00")
                studyProgress.text = "${df.format(it.Progress * 100)}%"
            } else {
                studyProgress.text = "-"
            }
        }
    }

    private fun initView() {
        binding = FragmentMineBinding.inflate(layoutInflater)
        rootView = binding!!.root
        realNameTextView = binding!!.realName
        usernameTextView = binding!!.username
        avatarImageView = binding!!.avatar
        authentication = binding!!.authentication
        settingButton = binding!!.setting!!
        feedbackButton = binding!!.feedback
        aboutButton = binding!!.about!!
        goLoginButton = binding!!.goLogin!!
        courseHoursTotal = binding!!.courseHoursTotal!!
        courseHoursFinished = binding!!.courseHoursFinished!!
        studyProgress = binding!!.progress!!
        loginPanel = binding!!.loginPanel
        mainPanel = binding!!.mainPanel
        goLoginButton.setOnClickListener {
            navController.navigate(R.id.action_global_loginFragment)
        }
        authentication.setOnClickListener {
            var action = MineFragmentDirections.actionMineFragmentToAuthenticationIndexFragment()
            navController.navigate(action)
        }
        settingButton.setOnClickListener {
            var action = MineFragmentDirections.actionMineFragmentToSettingFragment()
            navController.navigate(action)
        }
        feedbackButton.setOnClickListener {
            navController.navigate(MineFragmentDirections.actionMineFragmentToFeedbackAddFragment())
        }
        aboutButton.setOnClickListener {
            navController.navigate(MineFragmentDirections.actionMineFragmentToAboutFragment())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MineFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}