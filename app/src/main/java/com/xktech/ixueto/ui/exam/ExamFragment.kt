package com.xktech.ixueto.ui.exam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.xktech.ixueto.NavGraphDirections
import com.xktech.ixueto.databinding.FragmentExamBinding
import com.xktech.ixueto.model.Cookie
import com.xktech.ixueto.viewModel.ExamViewModel
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM_SUBJECT_ID = "subjectId"
private const val ARG_PARAM_PROFESSION_ID = "professionId"
private const val ARG_PARAM_GRADE_ID = "gradeId"

@AndroidEntryPoint
class ExamFragment : Fragment() {
    private var subjectId: Int = 0
    private var professionId: Int = 0
    private var gradeId: Int = 0
    private var binding: FragmentExamBinding? = null
    private lateinit var navController: NavController
    private var isFirstLoad = true
    private var rootView: View? = null
    private val examViewModel: ExamViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var testExamEnterButton: Button
    private lateinit var formalExamEnterButton: Button
    private lateinit var testExamDescriptionTextView: TextView
    private lateinit var formalExamDescriptionTextView: TextView
    private lateinit var loadingView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectId = it.getInt(ARG_PARAM_SUBJECT_ID)
            professionId = it.getInt(ARG_PARAM_PROFESSION_ID)
            gradeId = it.getInt(ARG_PARAM_GRADE_ID)
        }
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

    companion object {
        @JvmStatic
        fun newInstance(subjectId: Int, professionId: Int, gradeId: Int) =
            ExamFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_SUBJECT_ID, subjectId)
                    putInt(ARG_PARAM_PROFESSION_ID, professionId)
                    putInt(ARG_PARAM_GRADE_ID, gradeId)
                }
            }
    }

    private fun initView() {
        binding = FragmentExamBinding.inflate(layoutInflater)
        navController = findNavController()
        rootView = binding!!.root
        testExamEnterButton = binding!!.testExamEnter
        formalExamEnterButton = binding!!.formalExamEnter
        testExamDescriptionTextView = binding!!.testExamDescription
        formalExamDescriptionTextView = binding!!.formalExamDescription
        loadingView = binding!!.loading
        testExamEnterButton.setOnClickListener {
            examEnter(0)
        }
        formalExamEnterButton.setOnClickListener {
            examEnter(1)
        }
        val observer: ViewTreeObserver = formalExamDescriptionTextView.viewTreeObserver
        observer.addOnGlobalLayoutListener {
            var testExamDescriptionLp = testExamDescriptionTextView.layoutParams.apply {
                height = formalExamDescriptionTextView.height
            }
            testExamDescriptionTextView.layoutParams = testExamDescriptionLp
        }
    }

    private fun examEnter(examType: Int) {
        loadingView.visibility = View.VISIBLE
        examViewModel.isAllowEntry(subjectId, examType)
            .observe(viewLifecycleOwner) { isAllowEntryResult ->
                examViewModel.initExam(subjectId, examType).observe(this) {
                    loadingView.visibility = View.GONE
                    if (isAllowEntryResult.IsSuccess) {
                        userViewModel.getUserInfo()
                            .observe(viewLifecycleOwner) { userInfo ->
                                val webExamType =
                                    when (examType) {
                                        0 -> 2
                                        1 -> 0
                                        else -> {
                                            2
                                        }
                                    }
                                val url =
                                    "https://kcapp.ixueto.com/AppOnlineKs/myexamRoom_m.aspx?kstype=${webExamType}&uname_en=${userInfo.webToken}&jgid=${professionId}&ztlevel=${gradeId}"
                                var cookies = arrayOf(
                                    Cookie(
                                        "kcapp.ixueto.com",
                                        "szxy_username=${userInfo.desUsername!!}"
                                    ),
                                    Cookie(
                                        "kcapp.ixueto.com",
                                        "szxy_usertype=${userInfo.desUserType!!}"
                                    )
                                )
                                navController.navigate(
                                    NavGraphDirections.actionGlobalWebFragment(
                                        "考试",
                                        url,
                                        cookies
                                    )
                                )
                            }
                    } else {
                        Snackbar.make(
                            rootView!!,
                            "进入考试失败，${isAllowEntryResult.ErrorMessage}！",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}