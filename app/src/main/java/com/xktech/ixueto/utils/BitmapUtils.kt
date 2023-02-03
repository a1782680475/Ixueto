package com.xktech.ixueto.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.core.content.ContextCompat

object BitmapUtils {
    private fun getBitmap(
        vectorDrawable: VectorDrawable,
        sideLength: Int?,
        tintColor: Int? = null
    ): Bitmap? {
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
            if (vectorDrawable.intrinsicWidth < vectorDrawable.intrinsicHeight) {
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
        tintColor?.let {
            vectorDrawable.setTint(tintColor)
        }
        vectorDrawable.draw(canvas)
        return bitmap
    }

    fun drawableToBitmap(
        context: Context,
        drawableId: Int,
        sideLength: Int?,
        tintColor: Int? = null
    ): Bitmap? {
        return when (val drawable: Drawable? = ContextCompat.getDrawable(context, drawableId)) {
            is BitmapDrawable -> {
                BitmapFactory.decodeResource(context.resources, drawableId)
            }

            is VectorDrawable -> {
                getBitmap(drawable, sideLength, tintColor)

            }

            else -> {
                throw IllegalArgumentException("unsupported drawable type")
            }
        }
    }

}