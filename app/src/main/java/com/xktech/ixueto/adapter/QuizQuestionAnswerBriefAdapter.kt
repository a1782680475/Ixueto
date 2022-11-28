package com.xktech.ixueto.adapter

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.ItemQuizQuestionAnswerBriefBinding
import com.xktech.ixueto.model.QuizQuestionAnswerBrief

class QuizQuestionAnswerBriefAdapter(
    private var context: Context,
    private var itemClick: (QuizQuestionAnswerBrief, ViewHolder) -> Unit,
) : PagingDataAdapter<QuizQuestionAnswerBrief, QuizQuestionAnswerBriefAdapter.ViewHolder>(
    QuizQuestionAnswerComparator) {
    var isNightMode = false
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        var binding: ItemQuizQuestionAnswerBriefBinding =
            ItemQuizQuestionAnswerBriefBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false)
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = getItem(position)
        initView(holder, question!!)
        holder.bind(question)
        ViewCompat.setTransitionName(holder.root, "quiz_question_item_$position")
    }

    private fun initView(holder: ViewHolder, question: QuizQuestionAnswerBrief){
        holder.question.text = "【${question.Type}】${question.Question}"
        question.IsRight.let {
            if(question.IsRight == true){
                holder.result.text = "正确"
                if(isNightMode){
                    holder.result.setTextColor(ContextCompat.getColor(context!!,
                        R.color.success_dark))
                }else{
                    holder.result.setTextColor(ContextCompat.getColor(context!!,
                        R.color.success))
                }
            }else{
                holder.result.text = "错误"
                if(isNightMode){
                    holder.result.setTextColor(ContextCompat.getColor(context!!,
                        R.color.error_dark))
                }else{
                    holder.result.setTextColor(ContextCompat.getColor(context!!,
                        R.color.error))
                }
            }
        }
    }

    object QuizQuestionAnswerComparator : DiffUtil.ItemCallback<QuizQuestionAnswerBrief>() {
        override fun areItemsTheSame(
            oldItem: QuizQuestionAnswerBrief,
            newItem: QuizQuestionAnswerBrief,
        ): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(
            oldItem: QuizQuestionAnswerBrief,
            newItem: QuizQuestionAnswerBrief,
        ): Boolean {
            return oldItem.Id == newItem.Id
        }
    }

    class ViewHolder(
        binding: ItemQuizQuestionAnswerBriefBinding,
        itemClick: (QuizQuestionAnswerBrief, ViewHolder) -> Unit,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        var root: View = binding.root
        var question: TextView = binding.question
        val result: TextView = binding.result
        private var _quizQuestionAnswerBrief: QuizQuestionAnswerBrief? = null
        fun bind(quizQuestionAnswerBrief: QuizQuestionAnswerBrief) {
            _quizQuestionAnswerBrief = quizQuestionAnswerBrief
        }

        var holder = this

        init {
            root.setOnClickListener {
                _quizQuestionAnswerBrief?.let {
                    itemClick(it, holder)
                }
            }
        }
    }
}