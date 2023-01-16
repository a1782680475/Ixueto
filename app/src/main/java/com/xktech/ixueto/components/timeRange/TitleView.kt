package com.xktech.ixueto.components.timeRange

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.google.android.material.shape.*
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.DimenUtils

class TitleView(
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
        val defaultDiam = DimenUtils.dp2px(context, 60f)
        val defaultLineHeight = DimenUtils.dp2px(context, 5f)
        val defaultTextSize = 13f
        val defaultPadding = DimenUtils.dp2px(context, 13f)
        val defaultRadius = DimenUtils.dp2px(context, 3f)
        val defaultBackgroundColor = ContextCompat.getColor(context, defaultBackgroundColorId)
        val defaultTextColor = ContextCompat.getColor(context, defaultTextColorId)
        val diam = defaultDiam.toInt()
        var textSize = defaultTextSize
        var backgroundColor = defaultBackgroundColor
        var textColor = defaultTextColor
        var lineHeight = -defaultLineHeight.toInt()
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
        this.setLineSpacing(lineHeight.toFloat(), 1f)
        this.gravity = Gravity.CENTER
        this.textAlignment = TEXT_ALIGNMENT_CENTER
        this.setPadding(padding, padding, padding, padding)
        this.setTextColor(textColor)
        val shapePathModel = ShapeAppearanceModel.builder()
            .setAllCorners(RoundedCornerTreatment())
            .setAllCornerSizes(defaultRadius)
            .setAllEdges(TicketEdgeTreatment(defaultRadius))
            .build()
        val backgroundDrawable = MaterialShapeDrawable(shapePathModel).apply {
            setTint(backgroundColor)
            paintStyle = Paint.Style.FILL_AND_STROKE
        }
        this.background = backgroundDrawable
    }

    override fun onDraw(canvas: Canvas) {
        //背景绘制
//        var width = this.width
//        var height = this.height
//        var radius: Float = width.toFloat()
//        if (height > width) {
//            radius = height.toFloat()
//        }
//        canvas.drawCircle(
//            (width / 2).toFloat(),
//            (height / 2).toFloat(),
//            radius / 2,
//            mBackgroundPaint
//        )
        //文字绘制
        super.onDraw(canvas)
    }


    override fun setBackgroundColor(color: Int) {
        mBackgroundPaint.color = color
    }

    class TicketEdgeTreatment(
        private val size: Float
    ): EdgeTreatment() {
        override fun getEdgePath(
            length: Float,
            center: Float,
            interpolation: Float,
            shapePath: ShapePath
        ) {
            val circleRadius = size * interpolation
            //shapePath.lineTo(center - circleRadius, 0f)
            shapePath.addArc(
                center - circleRadius,
                -circleRadius,
                center + circleRadius,
                circleRadius,
                180f,
                -180f
            )
            //shapePath.lineTo(length, 0f)
        }
    }
}