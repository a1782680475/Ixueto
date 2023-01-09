package com.xktech.ixueto.components.timeRange

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.DimenUtils

class LineView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val mLinePaint: Paint = Paint()
    private var isNightMode = false
    private var _width = 0f
    private var _height = 0f
    private var lineWidth = 0f

    init {
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        val attr = context.obtainStyledAttributes(attrs, R.styleable.LineView)
        val defaultLineWidth = 3f
        val defaultWidth = DimenUtils.dp2px(context, 20f)
        val defaultHeight = DimenUtils.dp2px(context, 8f)
        var defaultLineColorId: Int = R.color.md_theme_light_primary
        if (isNightMode) {
            defaultLineColorId = R.color.md_theme_dark_primary
        }
        val defaultLineColor = ContextCompat.getColor(context, defaultLineColorId)
        var lineColor: Int
        attr.let {
            lineColor =
                attr.getColor(R.styleable.LineView_lineColor, defaultLineColor)
            _width = attr.getDimension(R.styleable.LineView_width, defaultWidth)
            _height = attr.getDimension(R.styleable.LineView_height, defaultHeight)
            lineWidth = attr.getDimension(R.styleable.LineView_lineWidth, defaultLineWidth)
        }
        mLinePaint.style = Paint.Style.FILL
        mLinePaint.color = lineColor
        mLinePaint.strokeWidth = lineWidth
        mLinePaint.strokeCap = Paint.Cap.ROUND
        mLinePaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        val lineX = (this.width / 2).toFloat()
        canvas.drawLine(lineX, 0f, lineX, this.height.toFloat(), mLinePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(_width.toInt(), _height.toInt())
    }

    fun setLineColor(color: Int) {
        mLinePaint.color = color
    }
}