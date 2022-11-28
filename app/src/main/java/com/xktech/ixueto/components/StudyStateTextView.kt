package com.xktech.ixueto.components

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R

class StudyStateTextView(
    context: Context,
    private var type: StudyStateEnum,
    attrs: AttributeSet? = null,
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    private var isNightMode = false
    enum class StudyStateEnum {
        NOT_STARTED,
        STUDYING,
        FINISHED
    }
    init {
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        var colorId: Int = R.color.state_wait
        when (type.ordinal) {
            0 -> colorId = if (isNightMode) R.color.state_wait_dark else R.color.state_wait
            1 -> colorId = if (isNightMode) R.color.state_studying_dark else R.color.state_studying
            2 -> colorId = if (isNightMode) R.color.state_finished_dark else R.color.state_finished
        }
        this.setTextColor(ContextCompat.getColor(context,
            colorId))
        this.gravity = Gravity.CENTER
    }
}