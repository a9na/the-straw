package com.ryz.coffee.admin.fragment.order

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ryz.coffee.admin.fragment.order.tab.tab1.AlreadyPaid
import com.ryz.coffee.admin.fragment.order.tab.tab2.OrderPacked
import com.ryz.coffee.admin.fragment.order.tab.tab3.OrderSent
import com.ryz.coffee.admin.fragment.order.tab.tab4.OrderDone

class AdminOrderFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> {
                AlreadyPaid()
            }
            1 -> {
                OrderPacked()
            }
            2 -> {
                OrderSent()
            }
            3 -> {
                OrderDone()
            }
            else -> {
                Fragment()
            }
        }
    }

}