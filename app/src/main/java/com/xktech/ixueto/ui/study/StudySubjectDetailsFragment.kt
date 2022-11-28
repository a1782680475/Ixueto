package com.xktech.ixueto.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import com.xktech.ixueto.databinding.FragmentStudySubjectDetailsBinding
import com.xktech.ixueto.viewModel.StudySubjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton


@AndroidEntryPoint
class StudySubjectDetailsFragment : Fragment() {
    private var binding: FragmentStudySubjectDetailsBinding? = null
    private var subjectId: Int = 0
    private lateinit var classInfoView: MaterialCardView
    private lateinit var subjectInfoView: MaterialCardView
    private lateinit var classNameView: TextView
    private lateinit var trainTimeView: TextView
    private lateinit var examTimeView: TextView
    private lateinit var professionNameView: TextView
    private lateinit var subjectNameView: TextView
    private lateinit var unitNameView: TextView
    private lateinit var chapterNumberView: TextView
    private lateinit var courseHoursView: TextView
    private lateinit var studyProgressView: TextView
    private var isFirstLoad = true
    private var rootView: View? = null
    private val studySubjectModel: StudySubjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectId = it.getInt(ARG_PARAM_SUBJECT_ID)
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
        fun newInstance(subjectId: Int) =
            StudySubjectDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_SUBJECT_ID, subjectId)
                }
            }

        private const val ARG_PARAM_SUBJECT_ID = "subjectId"
    }

    fun initView() {
        binding = FragmentStudySubjectDetailsBinding.inflate(layoutInflater)
        rootView = binding!!.root
        classInfoView = binding!!.classInfo
        classNameView = binding!!.className
        trainTimeView = binding!!.trainTime
        examTimeView = binding!!.examTime
        professionNameView = binding!!.professionName
        subjectNameView = binding!!.subjectName
        unitNameView = binding!!.unitName
        chapterNumberView = binding!!.chapterNumber
        courseHoursView = binding!!.courseHours
        studyProgressView = binding!!.studyProgress
        subjectInfoView = binding!!.subjectInfo
        studySubjectModel.getStudySubjectDetails(subjectId).observe(this) {
            if (it.ClassInfo == null) {
                classInfoView.visibility = View.GONE
            } else {
                classInfoView.visibility = View.VISIBLE
                classNameView.text = it.ClassInfo.ClassName
                var withinStr = ""
                if (!it.ClassInfo.StartTime.isNullOrEmpty() || !it.ClassInfo.EndTime.isNullOrEmpty()) {
                    withinStr += it.ClassInfo.StartTime ?: "-"
                    withinStr += "\n~"
                    withinStr += it.ClassInfo.EndTime ?: "-"
                }
                if (!withinStr.isNullOrEmpty()) {
                    withinStr += "\n"
                }
                if (!it.ClassInfo.StartTimeEveryDay.isNullOrEmpty() || !it.ClassInfo.EndTimeEveryDay.isNullOrEmpty()) {
                    withinStr += "每日"
                    withinStr += it.ClassInfo.StartTimeEveryDay ?: "-"
                    withinStr += "~"
                    withinStr += it.ClassInfo.EndTimeEveryDay ?: "-"
                }
                trainTimeView.text = withinStr
                var examWithinStr = ""
                if (!it.ClassInfo.ExamStartTime.isNullOrEmpty() && !it.ClassInfo.ExamEndTime.isNullOrEmpty()) {
                    examWithinStr += it.ClassInfo.ExamStartTime ?: "-"
                    examWithinStr += "\n~"
                    examWithinStr += it.ClassInfo.ExamEndTime ?: "-"
                }
                examTimeView.text = examWithinStr
            }
            professionNameView.text = it.ProfessionName
            subjectNameView.text = it.SubjectName
            unitNameView.text = it.UnitName
            chapterNumberView.text = "${it.ChapterNumber}章节"
            courseHoursView.text = "${it.CourseHours}课时"
            if (it.StudyProgress != null) {
                studyProgressView.text = "${it.StudyProgress}%"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}