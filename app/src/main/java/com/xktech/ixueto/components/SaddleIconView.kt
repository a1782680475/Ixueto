package com.xktech.ixueto.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.BitmapUtils
import com.xktech.ixueto.utils.DimenUtils

class SaddleIconView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var icon: Int? = null
    private val mBackgroundPaint: Paint = Paint()
    private val mIconPaint: Paint = Paint()
    private var isNightMode = false
    private var _diam = 0f
    private var _padding = 0f
    private var _backgroundStartColor = 0
    private var _backgroundEndColor = 0
    private var _iconColor = 0
    private var _icon = 0

    init {
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        var defaultDiam = DimenUtils.dp2px(context, 36f)
        var defaultPadding = DimenUtils.dp2px(context, 8f)
        var defaultStartBackgroundColorId: Int = R.color.md_theme_light_primary
        var defaultEndBackgroundColorId: Int = R.color.md_theme_light_primary_linear_gradient_end
        var defaultIconColorId: Int = R.color.md_theme_light_onPrimary
        if (isNightMode) {
            defaultStartBackgroundColorId = R.color.md_theme_dark_primary
            defaultEndBackgroundColorId = R.color.md_theme_dark_primary_linear_gradient_end
            defaultIconColorId = R.color.md_theme_dark_onPrimary
        }
        val defaultStartBackgroundColor =
            ContextCompat.getColor(context, defaultStartBackgroundColorId)
        val defaultEndBackgroundColor = ContextCompat.getColor(context, defaultEndBackgroundColorId)
        val defaultIconColor = ContextCompat.getColor(context, defaultIconColorId)
        _diam = defaultDiam
        _padding = defaultPadding
        _backgroundStartColor = defaultStartBackgroundColor
        _backgroundEndColor = defaultEndBackgroundColor
        _iconColor = defaultIconColor
        mBackgroundPaint.isAntiAlias = true
        val attr = context?.obtainStyledAttributes(attrs, R.styleable.RoundedRectangleIconView)
        attr?.let {
            _icon = attr.getResourceId(R.styleable.RoundedRectangleIconView_icon, 0)
        }
    }

    override fun onDraw(canvas: Canvas) {
        //渐变背景绘制
        var colors = intArrayOf(_backgroundStartColor, _backgroundEndColor)
        var positions = floatArrayOf(0.5f, 1f)
        var radius = this.width / 5
        var linearGradient = LinearGradient(
            0f, 0f, this.width.toFloat(), 0f, colors, positions,
            Shader.TileMode.CLAMP
        )
        mBackgroundPaint.shader = linearGradient
        val bgBitMap = BitmapUtils.drawableToBitmap(context, R.drawable.ic_saddle, width)
        bgBitMap?.let {
            val mDestRect = Rect(0, 0, bgBitMap.width, bgBitMap.height)
            val mPositionRect =
                Rect(
                    0,
                    0,
                    bgBitMap.width,
                    bgBitMap.height
                )
            canvas.drawBitmap(bgBitMap, mDestRect, mPositionRect, mBackgroundPaint)
        }
        //图标绘制
        canvas.save()
        canvas.restore()
        val sideLength = this.width - 2 * _padding
        val bitMap = BitmapUtils.drawableToBitmap(context, _icon, sideLength.toInt(), _iconColor)
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