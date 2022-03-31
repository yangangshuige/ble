package com.example.test.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.example.test.R

class LoadingView(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    FrameLayout(context, attrs, defStyle) {
    private var tvMsg: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.plr_loading_view, this)
        tvMsg = findViewById(R.id.tv_msg)
    }

    fun setMessage(msg: String?) {
        if (TextUtils.isEmpty(msg)) {
            return
        }
        tvMsg.text = msg
    }
}