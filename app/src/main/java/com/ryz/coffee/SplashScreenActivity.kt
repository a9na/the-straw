package com.ryz.coffee

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.ryz.coffee.onboarding.OnBoardingWithLoginActivity

class SplashScreenActivity : AppCompatActivity() {

    // inisialisasi atribut
    private lateinit var tvCoffee: TextView
    private lateinit var tvBeans: TextView
    private lateinit var imgCoffee: ImageView
    private lateinit var coffeeAnimation: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // inisiaslisasi id
        tvCoffee = findViewById(R.id.tvTitleSplashScreen)
        tvBeans = findViewById(R.id.tvSubTitleSplashScreen)
        imgCoffee = findViewById(R.id.imgCoffee)
        coffeeAnimation = findViewById(R.id.coffeeAnimation)

        // set animation scale textview
        // memanggil method untuk mengatur animasi pada splashscreen
        scaleAnimation(tvCoffee)
        scaleAnimation(tvBeans)

        // cek versi SDK untuk mengatur warna status bar nya
        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        // handler untuk mengatur transisi ketika berpindah ke activity onBoarding
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnBoardingWithLoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        },4800)
    }

    // method untuk mengatur status bar
    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    // method scale animation textview
    private fun scaleAnimation(tvStartScale: TextView) {
        tvStartScale.scaleX = 0f
        tvStartScale.scaleY = 0f
        tvStartScale.animate().apply {
            duration = 1500
            scaleXBy(1.0f)
            scaleYBy(1.0f)
        }.start()
    }
}