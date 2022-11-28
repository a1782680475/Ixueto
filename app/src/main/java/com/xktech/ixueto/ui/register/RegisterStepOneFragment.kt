package com.xktech.ixueto.ui.register

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nulabinc.zxcvbn.Zxcvbn
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentRegisterStepOneBinding
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
    private lateinit var passwordScore: ProgressBar
    private lateinit var loading: LinearLayout
    private lateinit var passwordScoreCalculator: Zxcvbn
    private var isDark = false
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
        loading = binding!!.loading
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
                    passwordScoreContainer.visibility = View.GONE
                } else {
                    passwordScoreContainer.visibility = View.VISIBLE
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
                            passwordScore.progressTintList =
                                ColorStateList.valueOf(progressBarColor)
                        }
                    }
                }
                changeNextStepStateByFormVerify()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        nextStepButton.setOnClickListener {
            hiddenSoftKeyboard()
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

                                override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
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

    private fun hiddenSoftKeyboard(){
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