package com.xktech.ixueto.ui.setting

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nulabinc.zxcvbn.Zxcvbn
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentPasswordModifyBinding
import com.xktech.ixueto.model.LoginReasonEnum
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PasswordModifyFragment : Fragment() {
    private var binding: FragmentPasswordModifyBinding? = null
    private lateinit var navController: NavController
    private var isFirstLoad = true
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var originalPassword: TextInputLayout
    private lateinit var newPassword: TextInputLayout
    private lateinit var repeatNewPassword: TextInputLayout
    private lateinit var passwordScore: ProgressBar
    private lateinit var passwordScoreContainer: LinearLayout
    private lateinit var modifyButton: Button
    private lateinit var forgetPasswordButton: Button
    private lateinit var passwordScoreCalculator: Zxcvbn
    private val userViewModel: UserViewModel by activityViewModels()
    private var isDark = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isFirstLoad) {
            isFirstLoad = false
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        newPassword.editText?.addTextChangedListener(object : TextWatcher {
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
                changeSubmitStateByFormVerify()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        repeatNewPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                changeSubmitStateByFormVerify()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        newPassword.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                newPassword.isErrorEnabled = false
                newPassword.error = ""
            } else {
                passwordScoreContainer.visibility = GONE
                val passwordText = (v as EditText).text.toString().trim()
                if (passwordText.length < 6) {
                    newPassword.isErrorEnabled = true
                    newPassword.error = "密码不能短于6位"
                } else {
                    newPassword.isErrorEnabled = false
                }
            }
        }
        repeatNewPassword.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                repeatNewPassword.isErrorEnabled = false
                repeatNewPassword.error = ""
            } else {
                val passwordText = repeatNewPassword.editText?.text.toString().trim()
                if (passwordText.length < 6) {
                    repeatNewPassword.isErrorEnabled = true
                    repeatNewPassword.error = "密码不能短于6位"
                } else {
                    repeatNewPassword.isErrorEnabled = false
                    val newPasswordText = newPassword.editText?.text.toString().trim()
                    if (newPasswordText != passwordText) {
                        repeatNewPassword.isErrorEnabled = true
                        repeatNewPassword.error = "两次密码输入不一致"
                    } else {
                        repeatNewPassword.isErrorEnabled = false
                    }
                }
            }
        }
        modifyButton.setOnClickListener {
            hiddenSoftKeyboard()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("提交确认")
                .setMessage("您的账户密码即将修改，确定吗？")
                .setNegativeButton(resources.getString(R.string.cancel), null)
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    submitData()
                }
                .show()
        }
        forgetPasswordButton.setOnClickListener {
            userViewModel.getUserInfo().observe(viewLifecycleOwner) { userInfo ->
                var bundle = bundleOf(
                    "phone" to userInfo.phone,
                )
               navController?.navigate(
                    R.id.action_passwordModifyFragment_to_resetPasswordStepOneFragment,
                    bundle,
                    null,
                    null
                )
            }
        }
    }

    private fun initView() {
        binding = FragmentPasswordModifyBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar!!
        originalPassword = binding!!.originalPassword
        newPassword = binding!!.newPassword
        passwordScore = binding!!.passwordScore
        passwordScore.max = 5
        passwordScoreContainer = binding!!.passwordScoreContainer
        repeatNewPassword = binding!!.repeatNewPassword
        modifyButton = binding!!.modifyButton
        forgetPasswordButton = binding!!.forgetPasswordButton
        passwordScoreCalculator = Zxcvbn()
        isDark =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private suspend fun getPasswordScore(password: String) = withContext(Dispatchers.IO) {
        passwordScoreCalculator.measure(password).score
    }

    private fun hiddenSoftKeyboard(){
        view?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    private fun formVerify(): Boolean {
        var isPass = true
        val originalPasswordText = originalPassword.editText?.text.toString().trim()
        val newPasswordText = newPassword.editText?.text.toString().trim()
        val repeatNewPasswordText = repeatNewPassword.editText?.text.toString().trim()
        if (originalPasswordText.isNullOrEmpty()) {
            isPass = false
        }
        if (isPass && newPasswordText.length < 6) {
            isPass = false
        }
        if (isPass && repeatNewPasswordText.length < 6) {
            isPass = false
        }
        if (isPass && newPasswordText != repeatNewPasswordText) {
            isPass = false
        }
        return isPass
    }

    private fun changeSubmitStateByFormVerify() {
        modifyButton.isEnabled = formVerify()
    }

    private fun submitData() {
        val originalPasswordText = originalPassword.editText?.text.toString().trim()
        val newPasswordText = newPassword.editText?.text.toString().trim()
        userViewModel.modifyPassword(originalPasswordText, newPasswordText)
            .observe(viewLifecycleOwner) { result ->
                if (result.IsSuccess) {
                    val snackBar = Snackbar.make(
                        rootView!!,
                        "密码已修改，请重新登录",
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.addCallback(object : Snackbar.Callback() {
                        override fun onShown(sb: Snackbar) {
                            super.onShown(sb)
                        }

                        override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            userViewModel.logout()
                            var bundle = bundleOf(
                                "loginReason" to LoginReasonEnum.PASSWORD,
                            )
                            navController.navigate(
                                R.id.action_global_loginFragment,
                                bundle
                            )
                        }
                    })
                    snackBar.show()
                } else {
                    Snackbar.make(
                        rootView!!,
                        "${result.ErrorMessage}！",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            PasswordModifyFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}