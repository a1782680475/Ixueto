package com.xktech.ixueto.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.DimenUtils


class LockStateView(context: Context, attributeSet: AttributeSet? = null) :
    ConstraintLayout(context, attributeSet) {
    init {
        LayoutInflater.from(context).inflate(R.layout.lock_state, this, true)
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        elevation = DimenUtils.dp2px(context, 1f)
    }
}