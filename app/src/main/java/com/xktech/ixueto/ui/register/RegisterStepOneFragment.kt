package com.xktech.ixueto.ui.register

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nulabinc.zxcvbn.Zxcvbn
import com.xktech.ixueto.BuildConfig
import com.xktech.ixueto.NavGraphDirections
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentRegisterStepOneBinding
import com.xktech.ixueto.utils.DimenUtils
import com.xktech.ixueto.utils.ValidityUtils
import com.xktech.ixueto.viewModel.RegisterCountDownViewModel
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RegisterStepOneFragment : Fragment() {
    private lateinit var navController: NavController
    private var binding: FragmentRegisterStepOneBinding? = null
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var phoneText: TextInputLayout
    private lateinit var passwordText: TextInputLayout
    private lateinit var nextStepButton: Button
    private lateinit var passwordScoreContainer: ViewGroup
    private lateinit var passwordScore: LinearProgressIndicator
    private lateinit var loading: LinearLayout
    private lateinit var agreementButton: TextView
    private lateinit var agreementAgreeCheckBox: CheckBox
    private lateinit var agreementContainer: ViewGroup
    private lateinit var passwordScoreCalculator: Zxcvbn
    private var isDark = false
    private var passwordScoreContainerNormalHeight: Int = 0
    private val userViewModel: UserViewModel by viewModels()
    private val countDownViewModel: RegisterCountDownViewModel by activityViewModels()
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
        binding = FragmentRegisterStepOneBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        phoneText = binding!!.phone
        passwordText = binding!!.password
        passwordScoreContainer = binding!!.passwordScoreContainer
        nextStepButton = binding!!.nextStepButton
        passwordScore = binding!!.passwordScore
        passwordScore.max = 5
        passwordScoreContainer.tag = 0
        loading = binding!!.loading
        agreementButton = binding!!.agreementButton
        agreementAgreeCheckBox = binding!!.agreementAgreeCheckBox
        agreementContainer = binding!!.agreementContainer
        passwordScoreContainerNormalHeight = DimenUtils.dp2px(requireContext(), 30f).toInt()
        passwordScoreCalculator = Zxcvbn()
        isDark =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        val phone =
            arguments?.getString("phone")
        phoneText.isHintAnimationEnabled = false
        if (!phone.isNullOrEmpty()) {
            phoneText.editText?.setText(phone)
            nextStepButton.isEnabled = true
        }
        phoneText.isHintAnimationEnabled = true
        phoneText.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                changeNextStepStateByFormVerify()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        passwordText.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val passwordText = s.toString().trim()
                if (passwordText.isNullOrEmpty()) {
                    if (passwordScoreContainer.tag == 2) {
                        passwordScoreContainer.tag = 1
                        val vaHidden: ValueAnimator = ValueAnimator.ofFloat(
                            passwordScoreContainer.layoutParams.height.toFloat(),
                            0f
                        )
                        vaHidden.duration = 300
                        vaHidden.interpolator = AccelerateInterpolator()
                        vaHidden.addUpdateListener {
                            var lp = passwordScoreContainer.layoutParams
                            lp.height = (it.animatedValue as Float).toInt()
                            passwordScoreContainer.layoutParams = lp
                        }
                        vaHidden.doOnEnd {
                            passwordScoreContainer.tag = 0
                        }
                        vaHidden.start()
                    }
                } else {
                    if (passwordScoreContainer.tag == 0) {
                        passwordScoreContainer.tag = 1
                        val vaShow: ValueAnimator = ValueAnimator.ofFloat(
                            0f,
                            passwordScoreContainerNormalHeight.toFloat()
                        )
                        vaShow.duration = 300
                        vaShow.interpolator = AccelerateInterpolator()
                        vaShow.addUpdateListener {
                            var lp = passwordScoreContainer.layoutParams
                            lp.height = (it.animatedValue as Float).toInt()
                            passwordScoreContainer.layoutParams = lp
                        }
                        vaShow.doOnEnd {
                            passwordScoreContainer.tag = 2
                        }
                        vaShow.start()
                    }
                    lifecycleScope.launch {
                        val score = getPasswordScore(s.toString()) + 1
                        passwordScore.progress = score
                        val progressBarColor = when (score) {
                            0, 1 -> {
                                if (isDark)
                                    ContextCompat.getColor(
                                        context!!,
                                        R.color.score_grade_low_dark
                                    )
                                else
                                    ContextCompat.getColor(
                                        context!!,
                                        R.color.score_grade_low
                                    )
                            }
                            2, 3 -> {
                                if (isDark)
                                    ContextCompat.getColor(
                                        context!!,
                                        R.color.score_grade_middle_dark
                                    )
                                else
                                    ContextCompat.getColor(
                                        context!!,
                                        R.color.score_grade_middle
                                    )
                            }
                            4, 5 -> {
                                if (isDark)
                                    ContextCompat.getColor(
                                        context!!,
                                        R.color.score_grade_height_dark
                                    )
                                else
                                    ContextCompat.getColor(
                                        context!!,
                                        R.color.score_grade_height
                                    )
                            }
                            else -> {
                                null
                            }
                        }
                        progressBarColor?.let {
                            passwordScore.setIndicatorColor(progressBarColor)
                        }
                    }
                }
                changeNextStepStateByFormVerify()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        agreementButton.setOnClickListener {
            navController.navigate(
                NavGraphDirections.actionGlobalWebFragment(
                    "用户协议",
                    "${BuildConfig.REMOTE_DOMAIN}/Content/Protocol.html",
                    null
                )
            )
        }
        nextStepButton.setOnClickListener {
            hiddenSoftKeyboard()
            if (!agreementAgreeCheckBox.isChecked) {
                Snackbar.make(
                    rootView!!,
                    "请先阅读用户协议，若您同意请勾选后可继续",
                    Snackbar.LENGTH_SHORT
                ).show()
                startShakeAnimation(agreementContainer)
            } else {
                loading.visibility = View.VISIBLE
                val phone = phoneText.editText?.text.toString().trim()
                val password = passwordText.editText?.text.toString().trim()
                if (!countDownViewModel.isRunning) {
                    userViewModel.sendCodeForRegister(phone)
                        .observe(viewLifecycleOwner) { result ->
                            loading.visibility = View.GONE
                            if (result.IsSuccess) {
                                val snackBar = Snackbar.make(
                                    rootView!!,
                                    "短信验证码已发送，请注意查收",
                                    Snackbar.LENGTH_SHORT
                                )
                                snackBar.addCallback(object : Snackbar.Callback() {
                                    override fun onShown(sb: Snackbar) {
                                        super.onShown(sb)
                                    }

                                    override fun onDismissed(
                                        transientBottomBar: Snackbar,
                                        event: Int
                                    ) {
                                        super.onDismissed(transientBottomBar, event)
                                        toNextStep(phone, password)
                                    }
                                })
                                snackBar.show()
                            } else {
                                Snackbar.make(
                                    rootView!!,
                                    result.ErrorMessage,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    loading.visibility = View.GONE
                    toNextStep(phone, password)
                }
            }
        }
    }

    private fun startShakeAnimation(view: View) {
        val animation: Animation = android.view.animation.AnimationUtils.loadAnimation(requireContext(),R.anim.shake)
        view.startAnimation(animation)
    }

    private suspend fun getPasswordScore(password: String) = withContext(Dispatchers.IO) {
        passwordScoreCalculator.measure(password).score
    }

    private fun changeNextStepStateByFormVerify() {
        nextStepButton.isEnabled = formVerify()
    }

    private fun formVerify(): Boolean {
        var isPass = true
        val phoneText = phoneText.editText?.text.toString().trim()
        if (!ValidityUtils.isPhone(phoneText)) {
            isPass = false
        }
        val passwordText = passwordText.editText?.text.toString().trim()
        if (isPass && passwordText.length < 6) {
            isPass = false
        }
        return isPass
    }

    private fun toNextStep(phone: String, password: String) {
        var bundle = bundleOf(
            "phone" to phone,
            "password" to password
        )
        parentFragment?.findNavController()?.navigate(
            R.id.action_registerStepOneFragment_to_registerStepTwoFragment,
            bundle,
            null,
            null
        )
    }

    private fun hiddenSoftKeyboard() {
        view?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RegisterStepOneFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}