package com.xktech.ixueto.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentLoginBinding
import com.xktech.ixueto.model.LoginReasonEnum
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var loginButton: View
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
    private lateinit var forgotPasswordButton: Button
    private lateinit var registerButton: Button
    private var binding: FragmentLoginBinding? = null
    private var rootView: View? = null
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var savedStateHandle: SavedStateHandle
    var loginReason: LoginReasonEnum = LoginReasonEnum.OTHER
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun initView() {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        navController = findNavController()
        rootView = binding!!.root
        loginButton = binding!!.loginButton!!
        usernameText = binding!!.usernameEditText!!
        passwordText = binding!!.passwordEditText!!
        forgotPasswordButton = binding!!.forgotPasswordButton
        registerButton = binding!!.toRegisterButton
        passwordText.setOnEditorActionListener { _, _, _ ->
            toLogin()
            true
        }
        loginButton.setOnClickListener {
            toLogin()
        }
        forgotPasswordButton.setOnClickListener {
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToResetPasswordStepOneFragment())
        }
        registerButton.setOnClickListener {
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterStepOneFragment())
        }
    }

    private fun toLogin() {
        hiddenSoftKeyboard()
        var username = usernameText.text
        var password = passwordText.text
        if (username == null || username.isEmpty()) {
            usernameText.error = "请填写用户名"
        }
        if (password == null || password.isEmpty()) {
            passwordText.error = "请填写密码"
        }
        if (loginEnabledCheck()) {
            userViewModel.login(username.toString(), password.toString())
                .observe(viewLifecycleOwner) {
                    if (it) {
                        savedStateHandle[LOGIN_SUCCESSFUL] = true
                        when (loginReason) {
                            LoginReasonEnum.PASSWORD,
                            LoginReasonEnum.REGISTER -> {
                                navController.navigate(R.id.action_loginFragment_to_mineFragment)
                            }
                            else -> {
                                navController.popBackStack()
                            }
                        }
                    } else {
                        Snackbar.make(rootView!!, "登录失败，请检查用户名密码", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
        }
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
        savedStateHandle = navController.previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGIN_SUCCESSFUL] = false
        arguments?.getSerializable("loginReason")?.let {
            loginReason = arguments?.getSerializable("loginReason") as LoginReasonEnum
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LoginFragment()

        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    private fun loginEnabledCheck(): Boolean {
        return !(usernameText.text.isNullOrEmpty() || passwordText.text.isNullOrEmpty())
    }
    private fun hiddenSoftKeyboard(){
        view?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}