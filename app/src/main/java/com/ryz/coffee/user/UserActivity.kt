package com.ryz.coffee.user

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.ryz.coffee.R
import com.ryz.coffee.user.fragment.order.tab.OrderFragment
import com.ryz.coffee.user.fragment.profile.ProfileFragment
import com.ryz.coffee.user.fragment.favorite.FavoriteFragment
import com.ryz.coffee.user.fragment.home.HomeFragment
import nl.joery.animatedbottombar.AnimatedBottomBar

class UserActivity : AppCompatActivity() {

    private lateinit var bottomBar: AnimatedBottomBar
    private var doubleBackToExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        bottomBar = findViewById(R.id.bottom_bar)

        val select = intent.getBooleanExtra("selected", false)
        Log.d("Select", "$select")

        if (select) {
            val orderTrans: FragmentTransaction = supportFragmentManager.beginTransaction()
            orderTrans.replace(R.id.fragment_container, OrderFragment())
            orderTrans.commit()
            bottomBar.selectTabAt(1)
        } else {
            val homeTrans: FragmentTransaction = supportFragmentManager.beginTransaction()
            homeTrans.replace(R.id.fragment_container, HomeFragment())
            homeTrans.commit()
        }

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
                        transaction.replace(R.id.fragment_container, HomeFragment())
                    }

                    R.id.tab_order -> {
                        transaction.replace(R.id.fragment_container, OrderFragment())
                    }

                    R.id.tab_favorite -> {
                        transaction.replace(R.id.fragment_container, FavoriteFragment())

                    }
                    R.id.tab_profile -> {
                        transaction.replace(R.id.fragment_container, ProfileFragment())
                    }
                }
                transaction.commit()
            }
        })
    }

    override fun onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExit = true
        Toast.makeText(this@UserActivity, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExit = false
        }, 4000)
    }
}