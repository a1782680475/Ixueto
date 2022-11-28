package com.xktech.ixueto.ui.register

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentRegisterStepTwoBinding
import com.xktech.ixueto.ui.login.ResetPasswordStepTwoFragment
import com.xktech.ixueto.viewModel.RegisterCountDownViewModel
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterStepTwoFragment : Fragment() {
    private lateinit var navController: NavController
    private var rootView: View? = null
    private var binding: FragmentRegisterStepTwoBinding? = null
    private lateinit var toolBar: Toolbar
    private lateinit var codeText1: EditText
    private lateinit var codeText2: EditText
    private lateinit var codeText3: EditText
    private lateinit var codeText4: EditText
    private lateinit var codeText5: EditText
    private lateinit var codeText6: EditText
    private lateinit var submitButton: Button
    private lateinit var resendButton: Button
    private lateinit var phoneText: TextView
    private lateinit var countDownText: TextView
    private var codeTextList: List<EditText> = mutableListOf()
    private var codeTextListAndAdjacentMap: MutableMap<EditText, Pair<EditText?, EditText?>> =
        mutableMapOf()
    private val userViewModel: UserViewModel by viewModels()
    private val countDownViewModel: RegisterCountDownViewModel by activityViewModels()
    private var phone: String? = null
    private var password: String? = null
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
        password = arguments?.getString("password")
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
                        submitButton.isEnabled = true
                        val imm =
                            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    } else {
                        submitButton.isEnabled = false
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
        submitButton.setOnClickListener {
            hiddenSoftKeyboard()
            var code = getInputCode()
            userViewModel.register(phone.toString(), password.toString(), code)
                .observe(viewLifecycleOwner) { result ->
                    if (result.IsSuccess) {
                        navController.navigate(R.id.action_registerStepTwoFragment_to_registerSuccessfulFragment)
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

    private fun startCountDown() {
        if (!countDownViewModel.isRunning) {
            countDownText.text = "60秒后可重发。"
            countDownText.visibility = View.VISIBLE
            resendButton.visibility = View.GONE
            countDownViewModel.countDownSeconds = 60
            countDownViewModel.startCountDown()
        }
        countDownViewModel.setOnCountDownChangeListener { countDownSeconds, isFinished ->
            this.view?.post {
                if (!isFinished) {
                    countDownText.text = "${countDownSeconds}秒后可重发。"
                } else {
                    countDownText.visibility = View.GONE
                    resendButton.visibility = View.VISIBLE
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
        binding = FragmentRegisterStepTwoBinding.inflate(layoutInflater)
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
        submitButton = binding!!.submitButton
        resendButton.setOnClickListener {
            if (!countDownViewModel.isRunning) {
                userViewModel.sendCodeForRegister(phone!!)
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