package com.xktech.ixueto.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
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
        val bitMap = getBitmap(context, _icon, sideLength.toInt())
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

    private fun getBitmap(vectorDrawable: VectorDrawable, sideLength: Int?): Bitmap? {
        var bitmapWidth = 0
        var bitmapHeight = 0
        var drawableWidth = 0
        var drawableHeight = 0
        var drawableLeft = 0
        var drawableTop = 0
        if (sideLength == null) {
            bitmapWidth = vectorDrawable.intrinsicWidth
            bitmapHeight = vectorDrawable.intrinsicHeight
            drawableWidth = bitmapWidth
            drawableHeight = bitmapHeight
        } else {
            if (vectorDrawable.intrinsicWidth > vectorDrawable.intrinsicHeight) {
                drawableWidth = sideLength
                drawableHeight =
                    drawableWidth * (vectorDrawable.intrinsicHeight.toFloat() / vectorDrawable.intrinsicWidth.toFloat()).toInt()
                drawableTop = ((drawableWidth.toFloat() - drawableHeight.toFloat()) / 2).toInt()
            } else {
                drawableHeight = sideLength
                drawableWidth =
                    drawableHeight * (vectorDrawable.intrinsicWidth.toFloat() / vectorDrawable.intrinsicHeight.toFloat()).toInt()
                drawableLeft = ((drawableHeight.toFloat() - drawableWidth.toFloat()) / 2).toInt()
            }
            bitmapWidth = sideLength
            bitmapHeight = sideLength
        }
        val bitmap: Bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(drawableLeft, drawableTop, drawableWidth, drawableHeight)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private fun getBitmap(context: Context, drawableId: Int, sideLength: Int?): Bitmap? {
        val drawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
        return if (drawable is BitmapDrawable) {
            BitmapFactory.decodeResource(context.resources, drawableId)
        } else if (drawable is VectorDrawable) {
            getBitmap(drawable, sideLength)

        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }
}