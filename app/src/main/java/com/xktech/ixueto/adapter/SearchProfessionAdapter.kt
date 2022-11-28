package com.xktech.ixueto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.xktech.ixueto.databinding.ItemSearchProfessionBinding
import com.xktech.ixueto.model.Profession

class SearchProfessionAdapter(
    private val professions: List<Profession>,
    private var itemClick: (Profession, ViewHolder) -> Unit?
) :
    RecyclerView.Adapter<SearchProfessionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: ItemSearchProfessionBinding =
            ItemSearchProfessionBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profession = professions[position]
        holder.professionName.text = profession.Name
        holder.regionName.text = profession.RegionName
        ViewCompat.setTransitionName(holder.professionItem, "search_profession_item_$position")
        holder.bind(profession)
    }

    class ViewHolder(binding: ItemSearchProfessionBinding, itemClick: (Profession, ViewHolder) -> Unit?) :
        RecyclerView.ViewHolder(binding.root) {
        val professionItem: LinearLayout = binding.professionItem
        val professionName: TextView = binding.professionName
        val regionName: TextView = binding.regionName
        private var _profession: Profession? = null
        fun bind(profession: Profession) {
            _profession = profession
        }

        var holder = this

        init {
            professionItem.setOnClickListener {
                _profession?.let {
                    itemClick(it, holder)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return professions.size
    }
}