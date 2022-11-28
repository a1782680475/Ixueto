package com.xktech.ixueto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.xktech.ixueto.databinding.ItemRegionBinding
import com.xktech.ixueto.model.Region


class RegionAdapter(
    private val regions: List<Region>,
    private val currentRegionIndex: Int,
    private var selectedColor: Int
) :
    BaseAdapter() {
    override fun getCount(): Int {
        return regions.count()
    }

    override fun getItem(position: Int): Region {
        return regions[position]
    }

    override fun getItemId(position: Int): Long {
        return regions[position].Id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var binding: ItemRegionBinding =
            ItemRegionBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        val viewHolder = ViewHolder(binding)
        val region: Region = getItem(position)
        viewHolder.title.text = region.Name
        if (position == currentRegionIndex) {
            viewHolder.title.setTextColor(selectedColor)
        }
        return binding.root
    }

    class ViewHolder(binding: ItemRegionBinding) {
        val title: TextView = binding.regionTitle
    }
}