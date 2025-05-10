package com.ryz.coffee.user.fragment.order.tab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ryz.coffee.user.fragment.order.tab.tab1.NotYetPaidFragment
import com.ryz.coffee.user.fragment.order.tab.tab2.PackedFragment
import com.ryz.coffee.user.fragment.order.tab.tab3.SentFragment
import com.ryz.coffee.user.fragment.order.tab.tab4.DoneFragment
import com.ryz.coffee.user.fragment.order.tab.tab5.CanceledFragment

class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
       return when(position) {
            0 -> {
                NotYetPaidFragment()
            }
            1 -> {
                PackedFragment()
            }
            2 -> {
                SentFragment()
            }
            3 -> {
                DoneFragment()
            }
            4 -> {
                CanceledFragment()
            }
            else -> {
                Fragment()
            }
        }
    }
}