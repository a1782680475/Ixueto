package com.xktech.ixueto.ui.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentResetPasswordStepOneBinding
import com.xktech.ixueto.utils.ValidityUtils
import com.xktech.ixueto.viewModel.ResetPasswordCountDownViewModel
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordStepOneFragment : Fragment() {
    private lateinit var navController: NavController
    private var rootView: View? = null
    private var binding: FragmentResetPasswordStepOneBinding? = null
    private lateinit var toolBar: Toolbar
    private lateinit var phoneText: TextInputLayout
    private lateinit var nextStepButton: Button
    private lateinit var loading: LinearLayout
    private val userViewModel: UserViewModel by viewModels()
    private val countDownViewModel: ResetPasswordCountDownViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun initView() {
        binding = FragmentResetPasswordStepOneBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        phoneText = binding!!.phone
        nextStepButton = binding!!.nextStepButton
        loading = binding!!.loading
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
                val phoneText = s.toString().trim()
                nextStepButton.isEnabled = ValidityUtils.isPhone(phoneText)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        nextStepButton.setOnClickListener {
            hiddenSoftKeyboard()
            loading.visibility = View.VISIBLE
            val phone = phoneText.editText?.text.toString().trim()
            if (!countDownViewModel.isRunning) {
                userViewModel.sendCodeForResetPassword(phone)
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
                                    toNextStep(phone)
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
            }else{
                loading.visibility = View.GONE
                toNextStep(phone)
            }
        }
    }

    private fun toNextStep(phone: String) {
        var bundle = bundleOf(
            "phone" to phone,
        )
        parentFragment?.findNavController()?.navigate(
            R.id.action_resetPasswordStepOneFragment_to_resetPasswordStepTwoFragment,
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
            ResetPasswordStepOneFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}