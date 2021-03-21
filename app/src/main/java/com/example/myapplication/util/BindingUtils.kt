package com.example.myapplication.util

import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.myapplication.R

@BindingAdapter("buttonBackground")
fun changeButtonBackground(view: Button, isPressed: Boolean) {
    var colorId = R.color.color_active
    if (isPressed) {
        colorId = R.color.color_passive
    }
    view.setBackgroundColor(ContextCompat.getColor(view.context, colorId))
}

@BindingAdapter("connected")
fun changeConnection(view: Button, isConnected: Boolean) {
    if (isConnected) {
        view.text = view.context.resources.getString(R.string.connected)
    } else {
        view.text = view.context.resources.getString(R.string.connect)
    }
}