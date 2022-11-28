package com.xktech.ixueto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.xktech.ixueto.databinding.ItemAnswerSheetBinding
import com.xktech.ixueto.model.AnswerSheetItem


class AnswerSheetAdapter(
    private val questions: MutableList<AnswerSheetItem>,
    private val currentQuestionIndex: Int,
    private var selectedColor: Int
) :
    BaseAdapter() {
    override fun getCount(): Int {
        return questions.count()
    }

    override fun getItem(position: Int): AnswerSheetItem {
        return questions[position]
    }

    override fun getItemId(position: Int): Long {
        return questions[position].Id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var binding: ItemAnswerSheetBinding =
            ItemAnswerSheetBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        val viewHolder = ViewHolder(binding)
        val question: AnswerSheetItem = getItem(position)
        viewHolder.question.text = "${position + 1}、${question.Question}"
        if (question.IsAnswered) {
            viewHolder.question.setTextColor(selectedColor)
        }
        if (position == currentQuestionIndex) {
            viewHolder.flag.visibility = View.VISIBLE
        }
        return binding.root
    }

    class ViewHolder(binding: ItemAnswerSheetBinding) {
        val question: TextView = binding.question
        val flag: ImageView = binding.flag
    }
}