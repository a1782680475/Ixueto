package com.xktech.ixueto.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.ItemSubjectBinding
import com.xktech.ixueto.model.Subject

class SubjectAdapter(
    private var context: Context?,
    private val subjects: List<Subject>,
    private var itemClick: (Subject, ViewHolder) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    class ViewHolder(binding: ItemSubjectBinding, itemClick: (Subject, ViewHolder) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        val card: ConstraintLayout = binding.subjectCard
        val coverImage: ShapeableImageView = binding.subjectImage
        val title: TextView = binding.subjectTitle
        private var _subject: Subject? = null
        var holder = this
        fun bind(subject: Subject) {
            _subject = subject
        }
        init {
            binding.root.setOnClickListener {
                _subject?.let {
                    itemClick(it, holder)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemSubjectBinding =
            ItemSubjectBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = subjects[position]
        Glide.with(context!!).load(subject.CoverImageUrl).placeholder(R.drawable.ic_placeholder).transition(
            DrawableTransitionOptions().crossFade()).into(holder.coverImage)
        holder.title.text = subject.Name
        holder.bind(subject)
        ViewCompat.setTransitionName(holder.card, "index_subject_item_$position")
    }

    override fun getItemCount(): Int {
        return subjects.size
    }
}