package com.xktech.ixueto.adapter

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.ItemSelectorBinding
import com.xktech.ixueto.model.Selector

class SelectorAdapter(
    private val context: Context,
    private val list: MutableList<Selector>
) :
    BaseAdapter(), Filterable {
    private val selectedColor: Int by lazy {
        if (context!!.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            ContextCompat.getColor(context!!, R.color.md_theme_dark_primary)
        } else {
            ContextCompat.getColor(context!!, R.color.md_theme_light_primary)
        }
    }
    var value: String = ""
    override fun getCount(): Int {
        return list.count()
    }

    override fun getItem(position: Int): Selector {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var binding: ItemSelectorBinding =
            ItemSelectorBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        val viewHolder = ViewHolder(binding)
        val data: Selector = getItem(position)
        viewHolder.title.text = data.Title
        if (data.Value == value) {
            viewHolder.title.setTextColor(selectedColor)
        }
        return binding.root
    }

    class ViewHolder(binding: ItemSelectorBinding) {
        val title: TextView = binding.name
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                val filterResults = FilterResults()
                filterResults.values = charString
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {

            }

            override fun convertResultToString(resultValue: Any): CharSequence? {
                return (resultValue as Selector).Title
            }
        }
    }
}