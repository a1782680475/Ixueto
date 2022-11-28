package com.xktech.ixueto.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.xktech.ixueto.ui.index.ProfessionFragment

class IndexFragmentStateAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val regionIds: MutableList<Int>
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return regionIds.size
    }

    override fun createFragment(position: Int): Fragment {
        return ProfessionFragment.newInstance(regionIds[position])
    }
}