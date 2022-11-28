package com.xktech.ixueto.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.ItemProfessionBinding
import com.xktech.ixueto.model.Profession

class ProfessionAdapter(
    private var context: Context?,
    private var regionId: Int,
    private var itemClick: (Profession, ViewHolder) -> Unit
) :
    PagingDataAdapter<Profession, ProfessionAdapter.ViewHolder>(ProfessionComparator) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemProfessionBinding =
            ItemProfessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profession = getItem(position)
        holder.title.text = profession!!.Name
        holder.bind(profession)
        Glide.with(context!!).load(profession.CoverImageUrl).placeholder(R.drawable.ic_placeholder).transition(
            DrawableTransitionOptions().crossFade()).into(holder.cover)
        ViewCompat.setTransitionName(holder.card, "index_profession_card_${regionId}_$position")
    }

    class ViewHolder(binding: ItemProfessionBinding, itemClick: (Profession, ViewHolder) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        var card: View = binding.root
        var info: LinearLayout = binding.professionInfo
        val title: TextView = binding.professionTitle
        val cover: ShapeableImageView = binding.professionImage
        private var _profession: Profession? = null
        fun bind(profession: Profession) {
            _profession = profession
        }

        var holder = this

        init {
            card.setOnClickListener {
                _profession?.let {
                    itemClick(it, holder)
                }
            }
        }
    }

    object ProfessionComparator : DiffUtil.ItemCallback<Profession>() {
        override fun areItemsTheSame(oldItem: Profession, newItem: Profession): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: Profession, newItem: Profession): Boolean {
            return oldItem.Id == newItem.Id
        }
    }
}