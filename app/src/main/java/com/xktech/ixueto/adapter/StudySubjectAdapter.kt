package com.xktech.ixueto.adapter

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.xktech.ixueto.R
import com.xktech.ixueto.components.LockStateView
import com.xktech.ixueto.components.StateView
import com.xktech.ixueto.databinding.ItemStudySubjectBinding
import com.xktech.ixueto.datastore.Rule
import com.xktech.ixueto.model.StudyStateEnum
import com.xktech.ixueto.model.StudySubject
import com.xktech.ixueto.model.SubjectStudyRuleEnum
import com.xktech.ixueto.model.SubjectStudyState
import com.xktech.ixueto.utils.DimenUtils
import com.xktech.ixueto.utils.EnumUtils

class StudySubjectAdapter(
    private val context: Context,
    private val rule: Rule,
    private val subjectStudyState: SubjectStudyState,
    private var itemClick: (StudySubject, Boolean, ViewHolder) -> Unit,
) :
    PagingDataAdapter<StudySubject, StudySubjectAdapter.ViewHolder>(StudySubjectComparator) {
    private val iconMargin = DimenUtils.dp2px(context!!, 5f).toInt()

    class ViewHolder(
        binding: ItemStudySubjectBinding,
        itemClick: (StudySubject, Boolean, ViewHolder) -> Unit,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        val subject: View = binding.root
        val subjectImageContainer: RelativeLayout = binding.subjectImageContainer
        val subjectImage: ImageView = binding.subjectImage
        val professionName: TextView = binding.professionName
        val subjectName: TextView = binding.subjectName
        val progressContainer: LinearLayout = binding.progressContainer
        val studyProgress: TextView = binding.progress
        val studyState: TextView = binding.studyState
        private var _studySubject: StudySubject? = null
        private var _isAllowEntry: Boolean? = null
        fun bind(studySubject: StudySubject, isAllowEntry: Boolean) {
            _studySubject = studySubject
            _isAllowEntry = isAllowEntry
        }

        private var holder = this

        init {
            subject.setOnClickListener {
                itemClick(_studySubject!!, _isAllowEntry!!, holder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemStudySubjectBinding =
            ItemStudySubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val studySubject = getItem(position)
        var isNightMode = false
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        initView(holder, studySubject!!, isNightMode)
        ViewCompat.setTransitionName(holder.subject, "study_subject_item_$position")
    }

    object StudySubjectComparator : DiffUtil.ItemCallback<StudySubject>() {
        override fun areItemsTheSame(oldItem: StudySubject, newItem: StudySubject): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: StudySubject, newItem: StudySubject): Boolean {
            return oldItem.Id == newItem.Id
        }
    }

    private fun initView(holder: ViewHolder, studySubject: StudySubject, isNightMode: Boolean) {
        holder.professionName.text = studySubject.ProfessionName
        holder.subjectName.text = studySubject.SubjectName
        if (holder.subjectImageContainer.childCount > 1) {
            holder.subjectImageContainer.removeViews(1, holder.subjectImageContainer.childCount - 1)
        }
        var stateView: StateView = when (studySubject.ExamineState.toInt()) {
            0 -> createWaitState()
            1 -> createSuccessState()
            2 -> createErrorState()
            else -> createWaitState()
        }.apply {
            this.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = iconMargin
                leftMargin = iconMargin
            }
        }
        holder.subjectImageContainer.addView(stateView)
        if (studySubject.StudyState.toInt() == 0) {
            holder.progressContainer.visibility = View.GONE
        } else {
            holder.progressContainer.visibility = View.VISIBLE
            holder.studyProgress.text = "${studySubject.Progress}%"
        }

        var stateText = "未开始"
        var stateColorId = R.color.wait
        when (studySubject.StudyState.toInt()) {
            0 -> {
                stateText = "未开始"
                stateColorId = if (isNightMode) R.color.state_wait_dark else R.color.state_wait
            }
            1 -> {
                stateText = "学习中"
                stateColorId =
                    if (isNightMode) R.color.state_studying_dark else R.color.state_studying
            }
            2 -> {
                stateText = "学习完成"
                stateColorId =
                    if (isNightMode) R.color.state_finished_dark else R.color.state_finished
            }
        }
        val textColor = ContextCompat.getColor(context!!, stateColorId)
        holder.studyState.text = stateText
        holder.studyState.setTextColor(textColor)
        //准入判断
        val isAllowEntry = isAllowEntry(studySubject)
        if (!isAllowEntry) {
            var lockStateView = LockStateView(context).apply {
                this.layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    topMargin = iconMargin
                    rightMargin = iconMargin
                    addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                }
            }
            holder.subjectImageContainer.addView(lockStateView)
            holder.subject.isEnabled = false
        } else {
            holder.subject.isEnabled = true
        }
        Glide.with(context!!).load(studySubject.CoverImageUrl)
            .placeholder(R.drawable.ic_placeholder).transition(
            DrawableTransitionOptions().crossFade()
        ).into(holder.subjectImage)
        if (studySubject != null) holder.bind(studySubject, isAllowEntry)
    }

    private fun createWaitState(): StateView {
        return StateView(context, StateView.StateEnum.WAIT, "待审核")
    }

    private fun createSuccessState(): StateView {
        return StateView(context, StateView.StateEnum.SUCCESS, "审核通过")
    }

    private fun createErrorState(): StateView {
        return StateView(context, StateView.StateEnum.ERROR, "审核未通过")
    }

    private fun isAllowEntry(studySubject: StudySubject): Boolean {
        if (studySubject.ExamineState.toInt() != 1) {
            return false
        } else {
            when (EnumUtils.getSubjectStudyRule(rule.studyRule.subjectStudyRule.toShort())) {
                SubjectStudyRuleEnum.UNLIMITED -> {
                    return true
                }
                SubjectStudyRuleEnum.STUDY_FINISHED -> {
                    val studyStateEnum = subjectStudyState.CurrentState?.let {
                        EnumUtils.getStudyState(it)
                    }
                    if (subjectStudyState.CurrentId == studySubject.Id) {
                        return true
                    } else {
                        when (studyStateEnum) {
                            StudyStateEnum.NOT_STARTED, StudyStateEnum.STUDYING -> {
                                return false
                            }
                            null, StudyStateEnum.FINISHED -> {
                                if (subjectStudyState.NextId == studySubject.Id) {
                                    return true
                                }
                                return false
                            }
                        }
                    }
                }
                SubjectStudyRuleEnum.EXAM_QUALIFIED -> {
                    if (subjectStudyState.CurrentId == studySubject.Id) {
                        return true
                    } else {
                        when (subjectStudyState.CurrentExamIsFinished) {
                            null, true -> {
                                if (subjectStudyState.NextId == studySubject.Id) {
                                    return true
                                }
                                return false
                            }
                            false -> {
                                return false
                            }
                        }
                    }
                }
            }
        }
    }
}