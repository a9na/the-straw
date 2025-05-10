package com.ryz.coffee.user.fragment.profile

import android.text.Editable
import android.text.TextWatcher
import android.view.View

class GenericTextWatcher(private val view: View) : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(p0: Editable?) {
        val text = p0.toString()
        when (view.id) {
        }
    }
}