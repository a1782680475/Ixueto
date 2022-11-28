package com.xktech.ixueto.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xktech.ixueto.databinding.ItemSubjectDetailsCourseBinding
import com.xktech.ixueto.model.Course

class SubjectDetailsCourseAdapter(
    private val context: Context,
    private var itemClick: (Course, ViewHolder) -> Unit
) :
    PagingDataAdapter<Course, SubjectDetailsCourseAdapter.ViewHolder>(CourseComparator) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemSubjectDetailsCourseBinding =
            ItemSubjectDetailsCourseBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = getItem(position)
        holder.courseName.text = "${course!!.Rank}、${course!!.Name}"
        holder.time.text = course!!.TimeLength
        holder.bind(course)
    }

    class ViewHolder(binding: ItemSubjectDetailsCourseBinding, itemClick: (Course, ViewHolder) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        val courseName: TextView = binding.courseName
        val time: TextView = binding.courseTime
        private var _course: Course? = null
        fun bind(course: Course) {
            _course = course
        }

        var holder = this

        init {
            binding.root.setOnClickListener {
                _course?.let {
                    itemClick(it, holder)
                }
            }
        }
    }

    object CourseComparator : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.Id == newItem.Id
        }
    }
}