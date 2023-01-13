package com.xktech.ixueto.ui.study

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import com.xktech.ixueto.R
import com.xktech.ixueto.components.timeRange.TimeRangeView
import com.xktech.ixueto.data.local.converter.DateConverter
import com.xktech.ixueto.databinding.FragmentStudySubjectDetailsBinding
import com.xktech.ixueto.viewModel.StudySubjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class StudySubjectDetailsFragment : Fragment() {
    private var binding: FragmentStudySubjectDetailsBinding? = null
    private var subjectId: Int = 0
    private lateinit var classInfoView: MaterialCardView
    private lateinit var subjectInfoView: MaterialCardView
    private lateinit var classNameView: TextView
    private lateinit var professionNameView: TextView
    private lateinit var subjectNameView: TextView
    private lateinit var unitNameView: TextView
    private lateinit var chapterNumberView: TextView
    private lateinit var courseHoursView: TextView
    private lateinit var studyProgressView: TextView
    private lateinit var trainTimeRangeView: TimeRangeView
    private lateinit var examTimeRangeView: TimeRangeView
    private var isFirstLoad = true
    private var rootView: View? = null
    private val studySubjectModel: StudySubjectViewModel by viewModels()
    private var isNightMode = false
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
        if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        binding = FragmentStudySubjectDetailsBinding.inflate(layoutInflater)
        rootView = binding!!.root
        classInfoView = binding!!.classInfo
        classNameView = binding!!.className
        trainTimeRangeView = binding!!.trainTimeRange
        examTimeRangeView = binding!!.examTimeRange
        professionNameView = binding!!.professionName
        subjectNameView = binding!!.subjectName
        unitNameView = binding!!.unitName
        chapterNumberView = binding!!.chapterNumber
        courseHoursView = binding!!.courseHours
        studyProgressView = binding!!.studyProgress
        subjectInfoView = binding!!.subjectInfo
        var trainTimeRangePrimaryColorId: Int = R.color.md_theme_light_primary
        var examTimeRangePrimaryColorId: Int = R.color.md_theme_light_secondary
        if (isNightMode) {
            trainTimeRangePrimaryColorId = R.color.md_theme_dark_primary
            examTimeRangePrimaryColorId = R.color.md_theme_dark_secondary
        }
        studySubjectModel.getStudySubjectDetails(subjectId).observe(this) {
            if (it.ClassInfo == null) {
                classInfoView.visibility = View.GONE
            } else {
                classInfoView.visibility = View.VISIBLE
                classNameView.text = it.ClassInfo.ClassName
                var trainStartDateStr: String? = null
                var trainStartTimeStr: String? = null
                var trainEndDateStr: String? = null
                var trainEndTimeStr: String? = null
                var dailyStartTimeStr: String? = null
                var dailyEndTimeStr: String? = null
                if (!it.ClassInfo.StartTime.isNullOrEmpty()) {
                    var startTime = DateConverter.revertDate(it.ClassInfo.StartTime)
                    trainStartDateStr = dateToDateString(startTime)
                    trainStartTimeStr = dateToTimeString(startTime)
                }
                if (!it.ClassInfo.EndTime.isNullOrEmpty()) {
                    var endTime = DateConverter.revertDate(it.ClassInfo.EndTime)
                    trainEndDateStr = dateToDateString(endTime)
                    trainEndTimeStr = dateToTimeString(endTime)
                }
                if (!it.ClassInfo.StartTimeEveryDay.isNullOrEmpty()) {
                    dailyStartTimeStr = it.ClassInfo.StartTimeEveryDay
                }
                if (!it.ClassInfo.EndTimeEveryDay.isNullOrEmpty()) {
                    dailyEndTimeStr = it.ClassInfo.EndTimeEveryDay
                }
                trainTimeRangeView.timeRangeSetting.primaryColor =
                    ContextCompat.getColor(requireContext(), trainTimeRangePrimaryColorId)
                trainTimeRangeView.timeRangeSetting.title = "培训时间"
                trainTimeRangeView.timeRangeSetting.startDate = trainStartDateStr
                trainTimeRangeView.timeRangeSetting.startTime = trainStartTimeStr
                trainTimeRangeView.timeRangeSetting.endDate = trainEndDateStr
                trainTimeRangeView.timeRangeSetting.endTime = trainEndTimeStr
                trainTimeRangeView.timeRangeSetting.isDailyShow = true
                trainTimeRangeView.timeRangeSetting.dailyStartTime = dailyStartTimeStr
                trainTimeRangeView.timeRangeSetting.dailyEndTime = dailyEndTimeStr
                trainTimeRangeView.updateView()
                var examStartDateStr: String? = null
                var examStartTimeStr: String? = null
                var examEndDateStr: String? = null
                var examEndTimeStr: String? = null
                if (!it.ClassInfo.ExamStartTime.isNullOrEmpty()) {
                    var startTime = DateConverter.revertDate(it.ClassInfo.ExamStartTime)
                    examStartDateStr = dateToDateString(startTime)
                    examStartTimeStr = dateToTimeString(startTime)
                }
                if (!it.ClassInfo.ExamEndTime.isNullOrEmpty()) {
                    var endTime = DateConverter.revertDate(it.ClassInfo.ExamEndTime)
                    examEndDateStr = dateToDateString(endTime)
                    examEndTimeStr = dateToTimeString(endTime)
                }
                examTimeRangeView.timeRangeSetting.primaryColor =
                    ContextCompat.getColor(requireContext(), examTimeRangePrimaryColorId)
                examTimeRangeView.timeRangeSetting.title = "考试时间"
                examTimeRangeView.timeRangeSetting.startDate = examStartDateStr
                examTimeRangeView.timeRangeSetting.startTime = examStartTimeStr
                examTimeRangeView.timeRangeSetting.endDate = examEndDateStr
                examTimeRangeView.timeRangeSetting.endTime = examEndTimeStr
                examTimeRangeView.timeRangeSetting.isDailyShow = false
                examTimeRangeView.updateView()

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

    private fun dateToDateString(date: Date): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日")
        return sdf.format(date)
    }

    private fun dateToTimeString(date: Date): String {
        val sdf = SimpleDateFormat("HH:mm:ss")
        return sdf.format(date)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}