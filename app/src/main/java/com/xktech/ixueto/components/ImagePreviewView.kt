package com.xktech.ixueto.components

import android.content.Context
import android.widget.ImageView
import com.lxj.xpopup.core.CenterPopupView
import com.xktech.ixueto.R


class ImagePreviewView(context: Context, private val imageResourceId: Int) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_image_preview
    }

    override fun onCreate() {
        super.onCreate()
        findViewById<ImageView>(R.id.image_view).setImageResource(imageResourceId)
    }
}