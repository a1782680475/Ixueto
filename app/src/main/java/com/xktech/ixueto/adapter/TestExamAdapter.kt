package com.xktech.ixueto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xktech.ixueto.databinding.ItemTestexamBinding
import com.xktech.ixueto.model.TestExam

class TestExamAdapter(
    private val examList: List<TestExam>,
    private var examJoinClick: (TestExam, ViewHolder) -> Unit
) : RecyclerView.Adapter<TestExamAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemTestexamBinding =
            ItemTestexamBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding, examJoinClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val testExam = examList[position]
        holder.bind(testExam)
        holder.examName.text = testExam.ExamName
        holder.examTimeLength.text = "${testExam.ExamTimeMinutes ?: "-"}分钟"
        holder.examFullScore.text = "${testExam.FullScore}分"
        holder.examPassScore.text = "${testExam.PassScore}分"
        if (testExam.AllowSeeScore) {
            holder.examScore.text = "${testExam.Score ?: "-"}分"
        } else {
            holder.examScoreContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return examList.size
    }

    class ViewHolder(
        binding: ItemTestexamBinding,
        examJoinClick: (TestExam, ViewHolder) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        val examName = binding.examName
        val examTimeLength = binding.examTimeLength
        val examFullScore = binding.examFullScore
        val examPassScore = binding.examPassScore
        val examScoreContainer = binding.examScoreContainer
        val examScore = binding.examScore
        val examJoin = binding.examJoin as MaterialButton
        private var _testExam: TestExam? = null
        var holder = this
        fun bind(testExam: TestExam) {
            _testExam = testExam
        }

        init {
            examJoin.setOnClickListener {
                _testExam?.let {
                    examJoinClick(it, holder)
                }
            }
        }
    }
}