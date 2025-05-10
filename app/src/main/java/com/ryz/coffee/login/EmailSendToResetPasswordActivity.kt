package com.ryz.coffee.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ryz.coffee.R
import com.ryz.coffee.onboarding.OnBoardingWithLoginActivity

class EmailSendToResetPasswordActivity : AppCompatActivity() {

    private lateinit var imgBack: ImageView
    private lateinit var btnOpenGmail: Button
    private lateinit var tvSkip: TextView
    private lateinit var tvAnotherEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_send_to_reset_password)

        imgBack = findViewById(R.id.imgBack)
        btnOpenGmail = findViewById(R.id.btnOpenGmail)
        tvSkip = findViewById(R.id.tvSkip)
        tvAnotherEmail = findViewById(R.id.tvAnotherEmail)

        imgBack.setOnClickListener {
            backToLogin()
        }

        btnOpenGmail.setOnClickListener {
            startActivityGmail(this, "com.google.android.gm")
        }

        tvSkip.setOnClickListener {
            startActivity(Intent(this@EmailSendToResetPasswordActivity,
                OnBoardingWithLoginActivity::class.java))
            finish()
        }

        tvAnotherEmail.setOnClickListener {
            startActivity(Intent(this@EmailSendToResetPasswordActivity,
                ForgotPasswordActivity::class.java))
            finish()
        }
    }

    private fun startActivityGmail(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            val intentMarket = Intent(Intent.ACTION_VIEW)
            intentMarket.data = Uri.parse("market://details?id=$packageName")
        }
        intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun backToLogin() {
        startActivity(Intent(this@EmailSendToResetPasswordActivity,
            OnBoardingWithLoginActivity::class.java))
        finish()
    }
}