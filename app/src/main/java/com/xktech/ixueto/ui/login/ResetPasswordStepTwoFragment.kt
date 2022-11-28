package com.xktech.ixueto.ui.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentResetPasswordStepTwoBinding
import com.xktech.ixueto.viewModel.ResetPasswordCountDownViewModel
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordStepTwoFragment : Fragment() {
    private lateinit var navController: NavController
    private var rootView: View? = null
    private var binding: FragmentResetPasswordStepTwoBinding? = null
    private lateinit var toolBar: Toolbar
    private lateinit var codeText1: EditText
    private lateinit var codeText2: EditText
    private lateinit var codeText3: EditText
    private lateinit var codeText4: EditText
    private lateinit var codeText5: EditText
    private lateinit var codeText6: EditText
    private lateinit var nextStepButton: Button
    private lateinit var resendButton: Button
    private lateinit var phoneText: TextView
    private lateinit var countDownText: TextView
    private var codeTextList: List<EditText> = mutableListOf()
    private var codeTextListAndAdjacentMap: MutableMap<EditText, Pair<EditText?, EditText?>> =
        mutableMapOf()
    private val userViewModel: UserViewModel by viewModels()
    private val countDownViewModel: ResetPasswordCountDownViewModel by activityViewModels()
    private var phone: String? = null
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
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        startCountDown()
        phone =
            arguments?.getString("phone")
        phoneText.text = "请输入发送到 $phone 的六位验证码。"
        val listIterator = codeTextList.listIterator()
        while (listIterator.hasNext()) {
            val editText = listIterator.next()
            val currentIndex = listIterator.nextIndex()
            var prevEditText: EditText? = null
            var nextEditText: EditText? = null
            if (currentIndex - 1 > 0) {
                prevEditText = codeTextList[currentIndex - 2]
            } else {
                editText?.isFocusable = true
                editText?.isFocusableInTouchMode = true
                editText?.requestFocus()
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, 0)
            }
            if (currentIndex < codeTextList.size) {
                nextEditText = codeTextList[currentIndex]
            }
            codeTextListAndAdjacentMap[editText] = Pair(prevEditText, nextEditText)
        }
        for (codeTextListAndAdjacentPair in codeTextListAndAdjacentMap) {
            val editText = codeTextListAndAdjacentPair.key
            var prevEditText: EditText? = codeTextListAndAdjacentPair.value.first
            var nextEditText: EditText? = codeTextListAndAdjacentPair.value.second
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.toString().trim().isNullOrEmpty()) {
                        nextEditText?.isFocusable = true
                        nextEditText?.isFocusableInTouchMode = true
                        nextEditText?.requestFocus()
                    }
                    if (checkInput()) {
                        nextStepButton.isEnabled = true
                        val imm =
                            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    } else {
                        nextStepButton.isEnabled = false
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })
            editText.setOnKeyListener { _, keyCode, _ ->
                val content: String = editText.text.toString()
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (content.isNullOrEmpty()) {
                        prevEditText?.isFocusable = true
                        prevEditText?.isFocusableInTouchMode = true
                        prevEditText?.requestFocus()
                    }
                }
                false
            }
        }
        nextStepButton.setOnClickListener {
            hiddenSoftKeyboard()
            var code = getInputCode()
            userViewModel.checkPhoneCode(phone.toString(), code)
                .observe(viewLifecycleOwner) { result ->
                    if (result.IsSuccess) {
                        val resetPwdToken = result.Token
                        var bundle = bundleOf(
                            "resetPwdToken" to resetPwdToken,
                        )
                        parentFragment?.findNavController()?.navigate(
                            R.id.action_resetPasswordStepTwoFragment_to_resetPasswordStepThirdFragment,
                            bundle,
                            null,
                            null
                        )
                    } else {
                        Snackbar.make(
                            rootView!!,
                            result.ErrorMessage.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun startCountDown() {
        if (!countDownViewModel.isRunning) {
            countDownText.text = "60秒后可重发。"
            countDownText.visibility = VISIBLE
            resendButton.visibility = GONE
            countDownViewModel.countDownSeconds = 60
            countDownViewModel.startCountDown()
        }
        countDownViewModel.setOnCountDownChangeListener { countDownSeconds, isFinished ->
            this.view?.post {
                if (!isFinished) {
                    countDownText.text = "${countDownSeconds}秒后可重发。"
                } else {
                    countDownText.visibility = GONE
                    resendButton.visibility = VISIBLE
                }
            }
        }
    }

    private fun checkInput(): Boolean {
        for (codeText in codeTextList) {
            if (codeText.text.trim().isNullOrEmpty()) {
                return false
            }
        }
        return true
    }

    private fun getInputCode(): String {
        var inputCode = ""
        for (codeText in codeTextList) {
            inputCode += codeText.text.trim()
        }
        return inputCode
    }

    private fun initView() {
        binding = FragmentResetPasswordStepTwoBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        phoneText = binding!!.phoneText
        codeText1 = binding!!.codeText1
        codeText2 = binding!!.codeText2
        codeText3 = binding!!.codeText3
        codeText4 = binding!!.codeText4
        codeText5 = binding!!.codeText5
        codeText6 = binding!!.codeText6
        countDownText = binding!!.countDownText
        resendButton = binding!!.resendButton
        codeTextList =
            mutableListOf(codeText1, codeText2, codeText3, codeText4, codeText5, codeText6)
        nextStepButton = binding!!.nextStepButton
        resendButton.setOnClickListener {
            if (!countDownViewModel.isRunning) {
                userViewModel.sendCodeForResetPassword(phone!!)
                    .observe(viewLifecycleOwner) { result ->
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
                                    startCountDown()
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
            }
        }
        if (countDownViewModel.isRunning) {
            countDownText.text = "${countDownViewModel.countDownSeconds}秒后可重发。"
        }
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
            ResetPasswordStepTwoFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}