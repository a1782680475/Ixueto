package com.xktech.ixueto.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.DimenUtils

class StateView(
    context: Context,
    private var type: StateEnum,
    private var text: String,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var mTextViewString = ""
    private var mTextColor: Int = ContextCompat.getColor(context!!, R.color.white)
    private val mTextViewSize = DimenUtils.dp2px(context, 13f)
    private val mTextViewPaint: Paint = Paint()
    private val mTextViewBound: Rect
    private val mBackgroundPaint: Paint = Paint()
    private var mBackgroundColor: Int = ContextCompat.getColor(context!!, R.color.wait)
    private var isNightMode = false

    enum class StateEnum {
        ERROR,
        SUCCESS,
        WAIT
    }

    init {
        this.elevation = 1f
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val defaultPadding = DimenUtils.dp2px(context, 12f).toInt()
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        val attr = context?.obtainStyledAttributes(attrs, R.styleable.StateView)
        var type = attr?.getInteger(R.styleable.StateView_stateType, type.ordinal)
        if (attr?.getString(R.styleable.StateView_stateText) != null)
            text = attr?.getString(R.styleable.StateView_stateText)!!
        var colorId: Int = R.color.wait
        when (type) {
            0 -> colorId = if (isNightMode) R.color.error_dark else R.color.error
            1 -> colorId = if (isNightMode) R.color.success_dark else R.color.success
            2 -> colorId = if (isNightMode) R.color.wait_dark else R.color.wait
        }
        mBackgroundColor = ContextCompat.getColor(context!!, colorId)
        if (text != null) {
            mTextViewString = text
        }
        mTextViewPaint.textSize = mTextViewSize
        mTextViewBound = Rect()
        mTextViewPaint.getTextBounds(mTextViewString, 0, mTextViewString.length, mTextViewBound)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //背景绘制
        mBackgroundPaint.color = mBackgroundColor
        canvas.save()
        canvas.restore()
        val radiusRight = measuredWidth - DimenUtils.dp2px(context, 30f)
        val radiusRightRect =
            RectF(radiusRight - 1, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        canvas.drawArc(radiusRightRect, 270f, 180f, false, mBackgroundPaint)
        canvas.save()
        canvas.restore()
        val centerLeft = DimenUtils.dp2px(context, 30f)
        val centerRect =
            RectF(centerLeft / 2, 0f, radiusRight + centerLeft / 2 + 1, measuredHeight.toFloat())
        canvas.drawRect(centerRect, mBackgroundPaint)
        canvas.save()
        canvas.restore()
        val radius = DimenUtils.dp2px(context!!, 2f)
        val lbRect =
            RectF(
                0f,
                measuredHeight.toFloat() / 2 - 1,
                centerLeft + radius,
                measuredHeight.toFloat()
            )
        canvas.drawRoundRect(lbRect, radius, radius, mBackgroundPaint)
        val ltRect = RectF(0f, 0f, centerLeft + 2, measuredHeight.toFloat() + radius)
        canvas.drawArc(ltRect, 180f, 90f, true, mBackgroundPaint)
        //文字绘制
        canvas.save()
        canvas.restore()
        mTextViewPaint.textAlign = Paint.Align.CENTER
        mTextViewPaint.color = mTextColor
        val fontMetrics = mTextViewPaint.fontMetricsInt
        val baseline = (measuredHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top
        val textRect = Rect(0, 0, measuredWidth, measuredHeight)
        canvas.drawText(
            mTextViewString,
            textRect.centerX().toFloat(),
            baseline.toFloat(),
            mTextViewPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val myWidth = setWidth(widthMode, widthMeasureSpec)
        val myHeight = setHeight(heightMode, heightMeasureSpec)
        setMeasuredDimension(myWidth, myHeight)
    }

    private fun setWidth(widthMode: Int, widthMeasureSpec: Int): Int {
        return if (widthMode == MeasureSpec.EXACTLY) {
            widthMeasureSpec
        } else {
            mTextViewBound.width() + paddingLeft + paddingRight
        }
    }

    private fun setHeight(heightMode: Int, heightMeasureSpec: Int): Int {
        return if (heightMode == MeasureSpec.EXACTLY) {
            heightMeasureSpec
        } else {
            mTextViewBound.height() + paddingTop + paddingBottom
        }
    }
}