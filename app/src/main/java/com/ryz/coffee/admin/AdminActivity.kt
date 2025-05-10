package com.ryz.coffee.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.ryz.coffee.R
import com.ryz.coffee.admin.fragment.home.AdminHomeFragment
import com.ryz.coffee.admin.fragment.order.AdminOrderFragment
import nl.joery.animatedbottombar.AnimatedBottomBar

class AdminActivity : AppCompatActivity() {

    private lateinit var bottomBar: AnimatedBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        bottomBar = findViewById(R.id.bottom_bar)

        val homeTrans: FragmentTransaction = supportFragmentManager.beginTransaction()
        homeTrans.replace(R.id.fragment_container, AdminHomeFragment())
        homeTrans.commit()

        bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {

            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab,
            ) {
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                when (newTab.id) {
                    R.id.tab_home -> {
                        transaction.replace(R.id.fragment_container, AdminHomeFragment())
                    }

                    R.id.tab_order -> {
                        transaction.replace(R.id.fragment_container, AdminOrderFragment())
                    }

                }
                transaction.commit()
            }
        })
    }
}