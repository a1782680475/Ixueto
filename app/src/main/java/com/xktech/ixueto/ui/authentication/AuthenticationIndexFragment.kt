package com.xktech.ixueto.ui.authentication

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.airbnb.lottie.LottieAnimationView
import com.xktech.ixueto.BuildConfig
import com.xktech.ixueto.NavGraphDirections
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentAuthenticationIndexBinding
import com.xktech.ixueto.model.AuthenticationStateEnum
import com.xktech.ixueto.utils.EnumUtils
import com.xktech.ixueto.viewModel.AuthenticationViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AuthenticationIndexFragment : Fragment() {
    private var binding: FragmentAuthenticationIndexBinding? = null
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var navController: NavController
    private lateinit var stateView: TextView
    private lateinit var nameView: TextView
    private lateinit var identityView: TextView
    private lateinit var idCardView: TextView
    private lateinit var unitView: TextView
    private lateinit var timeView: TextView
    private lateinit var infoView: ViewGroup
    private lateinit var placeholderView: LottieAnimationView
    private lateinit var startButton: Button
    private lateinit var loadingView: ViewGroup
    private lateinit var contentView: ViewGroup
    private lateinit var failView: ViewGroup
    private lateinit var failReasonView: TextView
    private lateinit var failReasonTimeView: TextView
    private lateinit var failTimeView: TextView
    private lateinit var ruleView: Button
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private var isDark = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        isDark =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        authenticationViewModel.getAuthenticationBrieInfo().observe(viewLifecycleOwner) {
            val authenticationState = EnumUtils.getAuthenticationState(it.AuthenticationState)
            when (authenticationState) {
                AuthenticationStateEnum.UN_AUTHENTICATION -> {
                    stateView.text = "未认证"
                    stateView.setTextColor(
                        if(isDark)
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.authenticate_normal_dark
                            )
                            else
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.authenticate_normal
                        )
                    )
                }
                AuthenticationStateEnum.AUTHENTICATED -> {
                    stateView.text = "已认证"
                    stateView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.authenticate_pass
                        )
                    )
                }
                AuthenticationStateEnum.AUTHENTICATING -> {
                    stateView.text = "认证中"
                    stateView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.authenticating
                        )
                    )
                }
                AuthenticationStateEnum.AUTHENTICATE_FAILED -> {
                    stateView.text = "认证失败"
                    stateView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.authenticate_not_pass
                        )
                    )
                }
                AuthenticationStateEnum.AUTHENTICATE_FOR_ARTIFICIAL -> {
                    stateView.text = "转人工认证"
                    stateView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.authenticating
                        )
                    )
                }
                else -> {

                }
            }
            when (authenticationState) {
                AuthenticationStateEnum.AUTHENTICATED -> {
                    placeholderView.visibility = View.GONE
                    failView.visibility = View.GONE
                    infoView.visibility = View.VISIBLE
                    nameView.text = it.Name
                    identityView.text = it.Identity
                    idCardView.text = it.IdCard
                    unitView.text = it.Unit
                    timeView.text = it.Time
                }
                AuthenticationStateEnum.AUTHENTICATE_FAILED -> {
                    placeholderView.visibility = View.GONE
                    infoView.visibility = View.GONE
                    failView.visibility = View.VISIBLE
                    failReasonView.text = it.FailReason
                    failTimeView.text = it.Time
                }
                else -> {
                    infoView.visibility = View.GONE
                    failView.visibility = View.GONE
                    placeholderView.visibility = View.VISIBLE
                }
            }
            if (authenticationState == AuthenticationStateEnum.UN_AUTHENTICATION) {
                startButton.text = "去认证"
            } else {
                startButton.text = "修改信息"
            }
            loadingView.visibility = View.GONE
            contentView.visibility = View.VISIBLE
        }
    }

    private fun initView() {
        binding = FragmentAuthenticationIndexBinding.inflate(layoutInflater)
        rootView = binding!!.root
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar = binding!!.toolBar!!
        toolBar.setupWithNavController(navController, appBarConfiguration)
        startButton = binding!!.startButton
        stateView = binding!!.stateView
        nameView = binding!!.nameView
        identityView = binding!!.identityView
        idCardView = binding!!.idCardView
        unitView = binding!!.unitView
        infoView = binding!!.infoView
        placeholderView = binding!!.placeholderView
        loadingView = binding!!.loadingView
        contentView = binding!!.contentView
        ruleView = binding!!.ruleView
        timeView = binding!!.timeView
        failTimeView = binding!!.failTimeView
        failReasonView = binding!!.failReasonView
        failView = binding!!.failView
        isDark =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        startButton.setOnClickListener {
            parentFragment?.findNavController()?.navigate(
                R.id.action_authenticationIndexFragment_to_authenticationStepOneFragment,
                null,
                null,
                null
            )
        }
        ruleView.setOnClickListener {
            navController.navigate(
                NavGraphDirections.actionGlobalWebFragment(
                    "实名认证",
                    "${BuildConfig.REMOTE_DOMAIN}/Content/AuthenticationInform.html",
                    null
                )
            )
        }
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>("isSubmitted").observe(currentBackStackEntry) {
            stateView.text = "认证中"
            stateView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.authenticating
                )
            )
            startButton.text = "修改信息"
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            AuthenticationIndexFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}