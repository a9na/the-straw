package com.ryz.coffee.admin.fragment.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ryz.coffee.R

class AdminOrderFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerTabLayout: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_order, container, false)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPagerTabLayout = view.findViewById(R.id.viewPagerTabLayout)

        val adapterViewPager = AdminOrderFragmentAdapter(activity?.supportFragmentManager!!, lifecycle)
        viewPagerTabLayout.apply {
            adapter = adapterViewPager
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        TabLayoutMediator(tabLayout, viewPagerTabLayout) { tab, position ->
            when(position) {
                0 -> {
                    tab.text = "Sudah bayar"
                }
                1 -> {
                    tab.text = "Dikemas"
                }
                2 -> {
                    tab.text = "Dikirim"
                }
                3 -> {
                    tab.text = "Selesai"
                }
            }
        }.attach()

        return view
    }
}