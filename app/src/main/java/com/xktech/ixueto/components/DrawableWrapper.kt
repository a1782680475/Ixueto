package com.xktech.ixueto.components

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

open class DrawableWrapper(drawable: Drawable) : Drawable(), Drawable.Callback {
    var wrappedDrawable: Drawable = drawable
        set(drawable) {
            field.callback = null
            field = drawable
            drawable.callback = this
        }

    override fun draw(canvas: Canvas) = wrappedDrawable.draw(canvas)

    override fun onBoundsChange(bounds: Rect) {
        wrappedDrawable.bounds = bounds
    }

    override fun setChangingConfigurations(configs: Int) {
        wrappedDrawable.changingConfigurations = configs
    }

    override fun getChangingConfigurations() = wrappedDrawable.changingConfigurations

    override fun setDither(dither: Boolean) = wrappedDrawable.setDither(dither)

    override fun setFilterBitmap(filter: Boolean) {
        wrappedDrawable.isFilterBitmap = filter
    }

    override fun setAlpha(alpha: Int) {
        wrappedDrawable.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        wrappedDrawable.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }


    override fun invalidateDrawable(who: Drawable) {

    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {

    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {

    }
}