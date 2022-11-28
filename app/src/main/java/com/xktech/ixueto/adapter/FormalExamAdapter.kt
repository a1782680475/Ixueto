package com.xktech.ixueto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xktech.ixueto.databinding.ItemFormalexamBinding
import com.xktech.ixueto.model.FormalExam

class FormalExamAdapter(
    private val examList: List<FormalExam>,
    private var examJoinClick: (FormalExam, ViewHolder) -> Unit
) : RecyclerView.Adapter<FormalExamAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemFormalexamBinding =
            ItemFormalexamBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding, examJoinClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val formalExam = examList[position]
        holder.bind(formalExam)
        holder.examName.text = formalExam.ExamName
        holder.admissionNumber.text = formalExam.AdmissionNumber
        holder.examTimeLength.text = "${formalExam.ExamTimeMinutes ?: "-"}分钟"
        holder.examFullScore.text = "${formalExam.FullScore}分"
        holder.examPassScore.text = "${formalExam.PassScore}分"
        if (formalExam.AllowSeeScore) {
            holder.examScore.text = "${formalExam.Score ?: "-"}分"
        } else {
            holder.examScoreContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return examList.size
    }

    class ViewHolder(
        binding: ItemFormalexamBinding,
        examJoinClick: (FormalExam, ViewHolder) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        val examName = binding.examName
        val admissionNumber = binding.admissionNumber
        val examTimeLength = binding.examTimeLength
        val examFullScore = binding.examFullScore
        val examPassScore = binding.examPassScore
        val examScoreContainer = binding.examScoreContainer
        val examScore = binding.examScore
        val examJoin = binding.examJoin
        private var _formalExam: FormalExam? = null
        var holder = this
        fun bind(formalExam: FormalExam) {
            _formalExam = formalExam
        }

        init {
            examJoin.setOnClickListener {
                _formalExam?.let {
                    examJoinClick(it, holder)
                }
            }
        }
    }
}