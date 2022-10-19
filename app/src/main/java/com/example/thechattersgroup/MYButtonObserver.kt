package com.example.thechattersgroup

import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView

class MYButtonObserver(private val button: ImageView) :TextWatcher{
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        if (p0.toString().trim().isNotEmpty()) {
            button.isEnabled = true
            button.setImageResource(R.drawable.ic_baseline_send_24)
        } else {
            button.isEnabled = false
            button.setImageResource(R.drawable.ic__send)
        }
    }

    override fun afterTextChanged(p0: Editable?) {
    }
}