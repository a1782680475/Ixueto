package com.xktech.ixueto.components.timeRange

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.DimenUtils

class RoundedRectangleTextView(
    context: Context,
    attrs: AttributeSet? = null,
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    private val mBackgroundPaint: Paint = Paint()
    private var isNightMode = false

    init {
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        val attr = context?.obtainStyledAttributes(attrs, R.styleable.CircleTextView)
        var defaultBackgroundColorId: Int = R.color.md_theme_light_primary
        var defaultTextColorId: Int = R.color.md_theme_light_onPrimary
        if (isNightMode) {
            defaultBackgroundColorId = R.color.md_theme_dark_primary
            defaultTextColorId = R.color.md_theme_dark_onPrimary
        }
        var defaultDiam = DimenUtils.dp2px(context, 20f)
        var defaultTextSize = 11f
        var defaultPadding = DimenUtils.dp2px(context, 1.5f)
        var defaultBackgroundColor = ContextCompat.getColor(context, defaultBackgroundColorId)
        var defaultTextColor = ContextCompat.getColor(context, defaultTextColorId)
        var diam = defaultDiam.toInt()
        var textSize = defaultTextSize
        var backgroundColor = defaultBackgroundColor
        var textColor = defaultTextColor
        var padding = defaultPadding.toInt()
        attr?.let {
            backgroundColor =
                attr.getColor(R.styleable.CircleTextView_backgroundColor, defaultBackgroundColor)
            textColor = attr.getColor(R.styleable.CircleTextView_textColor, defaultTextColor)
        }
        mBackgroundPaint.color = backgroundColor
        mBackgroundPaint.isAntiAlias = true
        this.width = diam
        this.height = diam
        this.textSize = textSize
        this.textAlignment = TEXT_ALIGNMENT_CENTER
        this.gravity = Gravity.CENTER
        this.setPadding(padding, padding, padding, padding)
        this.setTextColor(textColor)
    }

    override fun onDraw(canvas: Canvas) {
        //背景绘制
        var width = this.width
        var height = this.height
        if (height > width) {
            width = height
        }
        var radius = width / 4
        canvas.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            radius.toFloat(), radius.toFloat(),
            mBackgroundPaint
        )
        //文字绘制
        super.onDraw(canvas)
    }
    override fun setBackgroundColor(color: Int) {
        mBackgroundPaint.color = color
    }
}