package com.xktech.ixueto.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.DimenUtils

class QuestionResult(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val mBackgroundPaint: Paint = Paint()
    private val mBorderPaint: Paint = Paint()
    private val mTextPaint: Paint = Paint()
    private lateinit var mTextBound: Rect
    private var isNightMode = false
    private var _paddingLeft = 0f
    private var _paddingVertical = 0f
    private var _borderPadding = 0f
    private var _backgroundColor = 0
    private var _fontSize = 0f
    private var _borderWidth = 0f
    private var _arcWidth = 0f
    private var _type = -1
    private var _text = ""

    init {
        val attr = context?.obtainStyledAttributes(attrs, R.styleable.QuestionResultView)
        attr?.let {
            _type = attr.getInteger(R.styleable.QuestionResultView_resultType, -1)
        }
        initPreDrawMaterial()
    }

    override fun onDraw(canvas: Canvas) {
        //矩形绘制
        var rectLeft = 0f
        var rectTop = _borderWidth + _borderPadding
        var rectRight = _paddingLeft + mTextBound.width()
        var rectBottom =
            _borderPadding + _paddingVertical * 2 + mTextBound.height()
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, mBackgroundPaint)
        //外边框绘制（矩形部分）
        canvas.save()
        canvas.restore()
        var topBorderTop = _borderWidth
        var topBorderLeft = 0f
        var topBorderRight = rectRight
        canvas.drawLine(topBorderLeft, topBorderTop, topBorderRight, topBorderTop, mBorderPaint)
        canvas.save()
        canvas.restore()
        var bottomBorderLeft = topBorderLeft
        var bottomBorderRight = topBorderRight
        var bottomBorderBottom = rectBottom + _borderPadding
        canvas.drawLine(
            bottomBorderLeft,
            bottomBorderBottom,
            bottomBorderRight,
            bottomBorderBottom,
            mBorderPaint
        )
        //右弧形绘制
        canvas.save()
        canvas.restore()
        val arcRect =
            RectF(
                rectRight - _arcWidth - 1,
                rectTop,
                rectRight + _arcWidth - _borderWidth,
                rectBottom
            )
        canvas.drawArc(arcRect, -90f, 180f, true, mBackgroundPaint)
        //弧形边框绘制
        canvas.save()
        canvas.restore()
        val arcBorderRect =
            RectF(
                rectRight - _arcWidth - _borderPadding - 1,
                topBorderTop,
                rectRight + _arcWidth + _borderPadding - _borderWidth,
                bottomBorderBottom
            )
        canvas.drawArc(arcBorderRect, -90f, 180f, false, mBorderPaint)
        //文字绘制
        canvas.save()
        canvas.restore()
        val fontMetrics = mTextPaint.fontMetricsInt
        val baseLineY =
            (measuredHeight + fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas.drawText(
            _text,
            _paddingLeft,
            baseLineY.toFloat(),
            mTextPaint
        )
        super.onDraw(canvas)
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
        return mTextBound.width() + _paddingLeft.toInt() + _arcWidth.toInt() + _borderPadding.toInt() + _borderWidth.toInt()
    }

    private fun setHeight(heightMode: Int, heightMeasureSpec: Int): Int {
        return mTextBound.height() + _borderWidth.toInt() * 2 + _borderPadding.toInt() * 2 + _paddingVertical.toInt() * 2
    }

    var type: QuestionResult = QuestionResult.EMPTY
    set(value) {
        field = value
        when (value) {
            QuestionResult.EMPTY->{
                this._type = -1
            }
            QuestionResult.ERROR -> {
                this._type = 0
            }
            QuestionResult.RIGHT -> {
                this._type = 1
            }
        }
        initPreDrawMaterial()
        requestLayout()
        this.invalidate()
    }

    private fun initPreDrawMaterial() {
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        var defaultEmptyBackgroundColorId: Int = R.color.answer_empty
        var defaultRightBackgroundColorId: Int = R.color.answer_right
        var defaultErrorBackgroundColorId: Int = R.color.answer_error
        if (isNightMode) {
            defaultEmptyBackgroundColorId = R.color.answer_empty_dark
            defaultRightBackgroundColorId = R.color.answer_right_dark
            defaultErrorBackgroundColorId = R.color.answer_error_dark
        }
        var defaultBackgroundColor: Int = 0
        var defaultFontSize = DimenUtils.dp2px(context, 12f)
        var defaultBorderPadding = DimenUtils.dp2px(context, 5f)
        var defaultPaddingLeft = DimenUtils.dp2px(context, 8f)
        var defaultPaddingVertical = DimenUtils.dp2px(context, 8f)
        var defaultArcWidth = DimenUtils.dp2px(context, 15f)
        var defaultBorderWidth = 2f
        var defaultEmptyText = "未作答"
        var defaultRightText = "回答正确"
        var defaultErrorText = "回答错误"

        when (_type) {
            -1 -> {
                _text = defaultEmptyText
                defaultBackgroundColor =
                    ContextCompat.getColor(context, defaultEmptyBackgroundColorId)
            }
            0 -> {
                _text = defaultErrorText
                defaultBackgroundColor =
                    ContextCompat.getColor(context, defaultErrorBackgroundColorId)
            }
            1 -> {
                _text = defaultRightText
                defaultBackgroundColor =
                    ContextCompat.getColor(context, defaultRightBackgroundColorId)
            }
        }
        _backgroundColor = defaultBackgroundColor
        _borderPadding = defaultBorderPadding
        _paddingLeft = defaultPaddingLeft
        _paddingVertical = defaultPaddingVertical
        _arcWidth = defaultArcWidth
        _fontSize = defaultFontSize
        _borderWidth = defaultBorderWidth
        mBackgroundPaint.color = _backgroundColor
        mBackgroundPaint.isAntiAlias = true
        mTextPaint.color = Color.WHITE
        mTextPaint.textSize = _fontSize
        mTextBound = Rect()
        mTextPaint.getTextBounds(_text, 0, _text.length, mTextBound)
        mTextPaint.isAntiAlias = true
        mBorderPaint.color = _backgroundColor
        mBorderPaint.strokeWidth = _borderWidth
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
    }

    enum class QuestionResult {
        EMPTY,
        RIGHT,
        ERROR
    }
}