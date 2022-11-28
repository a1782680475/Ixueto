package com.xktech.ixueto.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xktech.ixueto.R
import com.xktech.ixueto.components.StudyStateTextView
import com.xktech.ixueto.databinding.ItemCourseBinding
import com.xktech.ixueto.datastore.Rule
import com.xktech.ixueto.model.CourseStudyBack
import com.xktech.ixueto.model.CourseStudyState
import com.xktech.ixueto.model.StudyCourse
import com.xktech.ixueto.model.StudyStateEnum
import com.xktech.ixueto.utils.EnumUtils

class StudyCourseAdapter(
    private val context: Context,
    private val rule: Rule,
    courseStudyState: CourseStudyState,
    private var itemClick: (StudyCourse, Boolean, ViewHolder) -> Unit,
) :
    PagingDataAdapter<StudyCourse, StudyCourseAdapter.ViewHolder>(CourseComparator) {
    private var _courseStudyState: CourseStudyState = courseStudyState
    private var _currentCourseStudyState: CourseStudyBack? = null
    fun updateCourseStudyState(courseStudyState: CourseStudyState) {
        _courseStudyState = courseStudyState
    }

    fun updateCurrentCourseStudyState(currentCourseStudyState: CourseStudyBack) {
        _currentCourseStudyState = currentCourseStudyState
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemCourseBinding =
            ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClick)
    }

    private fun crateStateView(
        parent: View,
        studyCourse: StudyCourse,
        stateViewEnum: StateViewEnum
    ): View? {
        when (stateViewEnum) {
            StateViewEnum.NORMAL -> {
                return null
            }
            StateViewEnum.NOT_ALLOW -> {
                var notAllowState = ImageView(context)
                notAllowState.layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftToLeft = parent.id
                    rightToRight = parent.id
                    topToTop = parent.id
                    bottomToBottom = parent.id
                }
                notAllowState.layoutParams.width = 53
                notAllowState.layoutParams.height = 53
                notAllowState.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_lock
                    )
                )
                DrawableCompat.setTint(
                    notAllowState.drawable,
                    ContextCompat.getColor(context, R.color.secondary_text)
                )
                return notAllowState
            }
            StateViewEnum.STUDYING -> {
                var studyingContainer = LinearLayout(context)
                studyingContainer.orientation = VERTICAL
                studyingContainer.layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftToLeft = parent.id
                    rightToRight = parent.id
                    topToTop = parent.id
                    bottomToBottom = parent.id
                }
                var studyingState =
                    StudyStateTextView(context, StudyStateTextView.StudyStateEnum.STUDYING)
                studyingState.text = "学习中"
                var process =
                    StudyStateTextView(context, StudyStateTextView.StudyStateEnum.STUDYING)
                process.text = "（${studyCourse.Progress.toInt()}%）"
                studyingContainer.addView(studyingState)
                studyingContainer.addView(process)
                return studyingContainer
            }
            StateViewEnum.FINISHED -> {
                var finishedState =
                    StudyStateTextView(context, StudyStateTextView.StudyStateEnum.FINISHED)
                finishedState.layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftToLeft = parent.id
                    rightToRight = parent.id
                    topToTop = parent.id
                    bottomToBottom = parent.id
                }
                finishedState.text = "已完成"
                return finishedState
            }
        }
        return null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = getItem(position)
        if (_currentCourseStudyState != null && course?.Id == _currentCourseStudyState?.courseId) {
            course?.StudyState = _currentCourseStudyState!!.studyState.value
            course?.Progress = _currentCourseStudyState!!.studyProgress
        }
        val checkResult = getStateView(holder.courseState, course!!)
        var courseState: View? = checkResult.first
        if (holder.courseState.childCount > 0) {
            holder.courseState.removeAllViews()
        }
        if (courseState != null) {
            holder.courseState.addView(courseState)
        }
        holder.courseName.text = "${course.Rank}、${course!!.Name}"
        holder.time.text = course!!.TimeLength
        holder.bind(course, checkResult.second)
        ViewCompat.setTransitionName(holder.root, "study_course_item_$position")
        ViewCompat.setTransitionName(holder.courseName, "study_courseName_item_$position")
    }

    class ViewHolder(
        binding: ItemCourseBinding,
        itemClick: (StudyCourse, Boolean, ViewHolder) -> Unit,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        var root: View = binding.root
        val courseState: ConstraintLayout = binding.courseState
        val courseName: TextView = binding.courseName
        val time: TextView = binding.courseTime
        private var _course: StudyCourse? = null
        private var _isAllowEntry: Boolean? = null
        fun bind(course: StudyCourse, isAllowEntry: Boolean) {
            _course = course
            _isAllowEntry = isAllowEntry
        }

        private var holder = this

        init {
            binding.root.setOnClickListener {
                _course?.let {
                    itemClick(it, _isAllowEntry!!, holder)
                }
            }
        }
    }

    private fun getStateView(
        parent: View,
        studyCourse: StudyCourse
    ): Pair<View?, Boolean> {
        var studyState = studyCourse?.let { EnumUtils.getStudyState(it.StudyState) }
        var isAllowEntry = true
        var stateViewEnum: StateViewEnum? = null
        if (rule.studyRule.isCourseRegularStudy) {
            if (_courseStudyState.CurrentState != null) {
                if (_courseStudyState.CurrentId == studyCourse.Id) {
                    stateViewEnum =
                        when (EnumUtils.getStudyState(_courseStudyState.CurrentState!!)) {
                            StudyStateEnum.NOT_STARTED -> {
                                StateViewEnum.NORMAL
                            }
                            StudyStateEnum.STUDYING -> {
                                StateViewEnum.STUDYING
                            }
                            StudyStateEnum.FINISHED -> {
                                StateViewEnum.FINISHED
                            }
                        }
                } else {
                    stateViewEnum = when (studyState) {
                        StudyStateEnum.NOT_STARTED -> {
                            if (_courseStudyState.CurrentState == StudyStateEnum.FINISHED.value && _courseStudyState.NextId == studyCourse.Id) {
                                StateViewEnum.NORMAL
                            } else {
                                isAllowEntry = false
                                StateViewEnum.NOT_ALLOW
                            }
                        }
                        StudyStateEnum.STUDYING -> {
                            StateViewEnum.STUDYING
                        }
                        StudyStateEnum.FINISHED -> {
                            StateViewEnum.FINISHED
                        }
                        else -> {
                            StateViewEnum.NORMAL
                        }
                    }
                }
            } else {
                if (_courseStudyState.NextId == studyCourse.Id) {
                    isAllowEntry = true
                    stateViewEnum = StateViewEnum.NORMAL
                } else {
                    isAllowEntry = false
                    stateViewEnum = StateViewEnum.NOT_ALLOW
                }
            }
            if (stateViewEnum == null) {
                stateViewEnum = StateViewEnum.NORMAL
            }
        } else {
            stateViewEnum = when (studyState) {
                StudyStateEnum.STUDYING -> {
                    StateViewEnum.STUDYING
                }
                StudyStateEnum.FINISHED -> {
                    StateViewEnum.FINISHED
                }
                else -> {
                    null
                }
            }
            if (_courseStudyState.CurrentId != null) {
                isAllowEntry = if (_courseStudyState.CurrentId == studyCourse.Id) {
                    stateViewEnum =
                        when (EnumUtils.getStudyState(_courseStudyState.CurrentState!!)) {
                            StudyStateEnum.NOT_STARTED -> {
                                StateViewEnum.NORMAL
                            }
                            StudyStateEnum.STUDYING -> {
                                StateViewEnum.STUDYING
                            }
                            StudyStateEnum.FINISHED -> {
                                StateViewEnum.FINISHED
                            }
                        }
                    true
                } else {
                    when (EnumUtils.getStudyState(_courseStudyState.CurrentState!!)) {
                        StudyStateEnum.NOT_STARTED -> {
                            false
                        }
                        StudyStateEnum.STUDYING -> {
                            false
                        }
                        StudyStateEnum.FINISHED -> {
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
            }
            if (studyState == StudyStateEnum.FINISHED) {
                isAllowEntry = true
            }
            if (stateViewEnum == null) {
                stateViewEnum = if (!isAllowEntry) {
                    StateViewEnum.NOT_ALLOW
                } else {
                    StateViewEnum.NORMAL
                }
            }
        }
        return Pair(crateStateView(parent, studyCourse, stateViewEnum), isAllowEntry)
    }

    enum class StateViewEnum {
        NORMAL,
        NOT_ALLOW,
        STUDYING,
        FINISHED
    }

    object CourseComparator : DiffUtil.ItemCallback<StudyCourse>() {
        override fun areItemsTheSame(oldItem: StudyCourse, newItem: StudyCourse): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: StudyCourse, newItem: StudyCourse): Boolean {
            return oldItem.Id == newItem.Id
        }
    }
}