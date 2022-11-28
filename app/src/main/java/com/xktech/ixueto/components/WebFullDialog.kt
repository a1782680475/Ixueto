package com.xktech.ixueto.components

import android.content.Context
import com.lxj.xpopup.impl.FullScreenPopupView
import com.xktech.ixueto.R


class WebFullDialog(context: Context, private val url: String) : FullScreenPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_web
    }

    override fun onCreate() {
        super.onCreate()
        findViewById<WebView>(R.id.web_view).apply {
            loadUrl(url)
        }
    }
}
