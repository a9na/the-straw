package com.ryz.coffee.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ryz.coffee.R

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var edEmailReset: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        mAuth = Firebase.auth

        edEmailReset = findViewById(R.id.edEmailReset)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val txtEmail = edEmailReset.text.toString()
            var isEmptyField = false

            if (TextUtils.isEmpty(txtEmail)) {
                isEmptyField = true
                edEmailReset.error = "Tidak boleh kosong"
            }

            if (!isEmptyField) {
                resetPassword(txtEmail)
                closeKeyboard()
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager? = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun resetPassword(txtEmail: String) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.sendPasswordResetEmail(txtEmail).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@ForgotPasswordActivity,
                    "Petunjuk untuk mengatur ulang kata sandi telah dikirim ke email anda",
                    Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@ForgotPasswordActivity,
                        EmailSendToResetPasswordActivity::class.java))
                    finish()
                }, 3000)

            } else {
                Toast.makeText(this@ForgotPasswordActivity,
                    task.exception!!.message,
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}