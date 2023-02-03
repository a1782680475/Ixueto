package com.xktech.ixueto.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.BitmapUtils
import com.xktech.ixueto.utils.DimenUtils

class CircleIconView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var icon: Int? = null
    private val mBackgroundPaint: Paint = Paint()
    private val mIconPaint: Paint = Paint()
    private var isNightMode = false
    private var _diam = 0f
    private var _padding = 0f
    private var _backgroundColor = 0
    private var _icon = 0

    init {
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        var defaultDiam = DimenUtils.dp2px(context, 100f)
        var defaultPadding = DimenUtils.dp2px(context, 20f)
        var defaultBackgroundColorId: Int = R.color.md_theme_light_primary
        if (isNightMode) {
            defaultBackgroundColorId = R.color.md_theme_dark_primary
        }
        val defaultBackgroundColor =
            ContextCompat.getColor(context, defaultBackgroundColorId)
        _diam = defaultDiam
        _padding = defaultPadding
        _backgroundColor = defaultBackgroundColor
        mBackgroundPaint.isAntiAlias = true
        val attr = context?.obtainStyledAttributes(attrs, R.styleable.CircleIconView)
        attr?.let {
            _icon = attr.getResourceId(R.styleable.CircleIconView_icon, R.drawable.ic_exam_test)
            _backgroundColor =
                attr.getColor(R.styleable.CircleIconView_backgroundColor, defaultBackgroundColor)
        }
    }

    override fun onDraw(canvas: Canvas) {
        //背景绘制
        mBackgroundPaint.color = _backgroundColor
        canvas.drawCircle(
            _diam / 2,
            _diam / 2,
            _diam / 2,
            mBackgroundPaint
        )
        //图标绘制
        canvas.save()
        canvas.restore()
        val sideLength = _diam - 2 * _padding
        val bitMap = BitmapUtils.drawableToBitmap(context, _icon, sideLength.toInt())
        bitMap?.let {
            var pLeft: Int
            var pTop: Int
            val bitMapWidth = bitMap.width
            val bitMapHeight = bitMap.height
            pLeft = if (sideLength < bitMapWidth) {
                _padding.toInt()
            } else {
                (this.width - bitMap.width) / 2
            }
            pTop = if (sideLength < bitMapHeight) {
                _padding.toInt()
            } else {
                (this.height - bitMap.height) / 2
            }
            val mPositionRect =
                Rect(
                    pLeft,
                    pTop,
                    pLeft + bitMapWidth,
                    pTop + bitMap.height
                )
            val mDestRect = Rect(0, 0, bitMap.width, bitMap.height)
            canvas.drawBitmap(bitMap, mDestRect, mPositionRect, mIconPaint)
        }
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(_diam.toInt(), _diam.toInt())
        setMeasuredDimension(_diam.toInt(), _diam.toInt())
    }
}