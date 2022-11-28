package com.xktech.ixueto.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class StudyFragmentStateAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, fragments :MutableList<Fragment>) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    private var  fragments :MutableList<Fragment> = fragments
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}