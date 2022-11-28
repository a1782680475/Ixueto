package com.xktech.ixueto.components


import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnKeyListener
import android.webkit.*
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.xktech.ixueto.R


class WebView constructor(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {

    private var progress: ProgressBar
    private var webView: WebView
    private var isLastLoadSuccess = false//是否成功加载完成过web，成功过后的网络异常 不改变web
    private var isError = false
    var cookieManager: CookieManager

    init {
        val rootView =
            LayoutInflater.from(context).inflate(R.layout.layout_web_progress_view, this, true)
        progress = rootView.findViewById(R.id.progress)
        webView = rootView.findViewById(R.id.my_web_view)
        webView.webChromeClient = MyWebChromeClient()
        webView.webViewClient = MyWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.defaultTextEncodingName = "UTF-8"
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.setSupportZoom(true)
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
        cookieManager = CookieManager.getInstance()
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        } else {
            cookieManager.setAcceptCookie(true);
        }
        // 点击后退按钮,让WebView后退一页(也可以覆写Activity的onKeyDown方法)
        webView.setOnKeyListener(OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack()
                    return@OnKeyListener true // 已处理
                }
            }
            false
        })
    }

    private inner class MyWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            setProgress(newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            if (title.contains("html")) {
                return
            }
            listener?.onTitle(title)
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.grant(request?.resources)
        }
    }

    private fun setProgress(newProgress: Int) {
        if (newProgress == 100) {
            progress.visibility = View.GONE
        } else {
            progress.progress = newProgress
        }
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            //在访问失败的时候会首先回调onReceivedError，然后再回调onPageFinished。
            if (!isError) {
                isLastLoadSuccess = true
                listener?.success()
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            //在访问失败的时候会首先回调onReceivedError，然后再回调onPageFinished。
            isError = true
            if (!isLastLoadSuccess) {//之前成功加载完成过，不会回调
                listener?.error()
            }
        }
    }

    /**
     * 千万不要更改这个 "SSDJsBirdge"  注意！！！！！
     */
    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(jsInterface: Any) {
        webView.addJavascriptInterface(jsInterface, "SSDJsBirdge")
    }

    fun reload() {
        isError = false
        webView.reload()
    }

    fun loadUrl(url: String) {
        isError = false
        try {
            webView.loadUrl(url)
        } catch (e: Exception) {

        }

    }

    fun canGoBack(): Boolean {
        val canGoBack = webView.canGoBack()
        if (canGoBack) {
            webView.goBack()
        }
        return canGoBack
    }

    /**
     * must be called on the main thread
     */
    fun destory() {
        try {
            webView.destroy()
        } catch (e: Exception) {
        }

    }

    private var listener: OnWebLoadStatusListener? = null

    fun setOnLoadStatueListener(listener: OnWebLoadStatusListener) {
        this.listener = listener
    }

    interface OnWebLoadStatusListener {
        fun error()

        fun success()

        fun onTitle(title: String)
    }
}