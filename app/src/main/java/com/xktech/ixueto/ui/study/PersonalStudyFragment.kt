package com.xktech.ixueto.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.StudySubjectAdapter
import com.xktech.ixueto.components.LCEERecyclerView
import com.xktech.ixueto.databinding.FragmentPersonalStudyBinding
import com.xktech.ixueto.datastore.Rule
import com.xktech.ixueto.di.NetworkModule
import com.xktech.ixueto.model.AuthenticationStateEnum
import com.xktech.ixueto.model.SubjectStudyState
import com.xktech.ixueto.viewModel.StudySubjectViewModel
import com.xktech.ixueto.viewModel.StudyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PersonalStudyFragment : Fragment() {
    private var binding: FragmentPersonalStudyBinding? = null
    private lateinit var list: LCEERecyclerView
    private var isFirstLoad = true
    private var rootView: View? = null
    private val studySubjectViewModel: StudySubjectViewModel by viewModels()
    private val studyViewModel: StudyViewModel by viewModels()
    private var rule: Rule? = null
    private var subjectStudyState: SubjectStudyState? = null
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (isFirstLoad) {
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        if (isFirstLoad) {
            isFirstLoad = false
            list.showLoadingView()
            if (!NetworkModule.token.isNullOrEmpty()) {
                studyViewModel.getRule().observe(viewLifecycleOwner) { rule ->
                    this.rule = rule
                    val layoutManager = LinearLayoutManager(context)
                    list.recyclerView.layoutManager = layoutManager
                    studyViewModel.getSubjectStudyCheckBasis()
                        .observe(viewLifecycleOwner) { subjectStudyCheckBasis ->
                            this.subjectStudyState = subjectStudyCheckBasis.subjectStudyState
                            val studySubjectAdapter =
                                StudySubjectAdapter(
                                    requireContext(),
                                    rule!!,
                                    subjectStudyCheckBasis.subjectStudyState
                                ) { studySubject, isAllowEntry, viewHolder ->
                                    if (subjectStudyCheckBasis.authenticationState != AuthenticationStateEnum.AUTHENTICATED) {
                                        Snackbar.make(
                                            rootView!!,
                                            "请在实名认证通过后进行学习",
                                            Snackbar.LENGTH_SHORT
                                        )
                                            .show()
                                        false
                                    } else if (!isAllowEntry) {
                                        false
                                    } else {
                                        val extras =
                                            FragmentNavigatorExtras(viewHolder.subject to "studySubject")
                                        var bundle = bundleOf("subjectId" to studySubject.Id)
                                        bundle.putInt(
                                            "professionId",
                                            studySubject.ProfessionId
                                        )
                                        bundle.putInt(
                                            "gradeId",
                                            studySubject.GradeId
                                        )
                                        bundle.putString(
                                            "subjectImage",
                                            studySubject.CoverImageUrl
                                        )
                                        bundle.putString(
                                            "professionName",
                                            studySubject.ProfessionName
                                        )
                                        bundle.putString(
                                            "subjectName",
                                            studySubject.SubjectName
                                        )
                                        bundle.putInt(
                                            "chapterNumber",
                                            studySubject.ChapterNumber
                                        )
                                        bundle.putInt("courseHours", studySubject.CourseHours)
                                        parentFragment?.findNavController()?.navigate(
                                            R.id.action_studyFragment_to_studySubjectFragment,
                                            bundle,
                                            null,
                                            extras
                                        )
                                    }
                                }
                            studySubjectAdapter.addLoadStateListener { loadState ->
                                if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && studySubjectAdapter.itemCount == 0) {
                                    list.showEmptyView()
                                } else {
                                    list.hideAllViews()
                                }
                            }
                            list.recyclerView.adapter = studySubjectAdapter
                            lifecycleScope.launch {
                                studySubjectViewModel.getPersonalStudySubjects().collectLatest {
                                    studySubjectAdapter.submitData(it)
                                }
                            }
                        }
                }
            }
            super.onViewCreated(view, savedInstanceState)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            PersonalStudyFragment()
    }

    private fun initView() {
        binding = FragmentPersonalStudyBinding.inflate(layoutInflater)
        rootView = binding!!.root
        list = binding!!.list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}