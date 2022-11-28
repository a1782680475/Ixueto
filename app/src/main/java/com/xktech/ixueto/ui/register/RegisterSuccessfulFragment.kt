package com.xktech.ixueto.ui.register

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentRegisterSuccessfulBinding
import com.xktech.ixueto.model.LoginReasonEnum

class RegisterSuccessfulFragment : Fragment() {
    private lateinit var navController: NavController
    private var rootView: View? = null
    private var binding: FragmentRegisterSuccessfulBinding? = null
    private lateinit var welcomeTitle: TextView
    private lateinit var welcomeSubTitle: TextView
    private lateinit var lottieView: LottieAnimationView
    private lateinit var operationContainer: LinearLayout
    private lateinit var startButton: Button
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

    private fun initView() {
        binding = FragmentRegisterSuccessfulBinding.inflate(layoutInflater)
        rootView = binding!!.root
        welcomeTitle = binding!!.welcomeTitle
        welcomeSubTitle = binding!!.welcomeSubtitle
        lottieView = binding!!.lottieView
        operationContainer = binding!!.operationContainer
        startButton = binding!!.startButton
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        lottieView.addAnimatorUpdateListener { animation ->
            val progress: Float = animation.animatedFraction
            if (progress == 1f) {
                operationContainer.animate().alpha(1f).setDuration(500)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            operationContainer.alpha = 1f
                        }
                    })
            }
        }
        startButton.setOnClickListener {
            var bundle = bundleOf(
                "loginReason" to LoginReasonEnum.REGISTER,
            )
            navController.navigate(R.id.action_global_loginFragment, bundle)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RegisterSuccessfulFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}