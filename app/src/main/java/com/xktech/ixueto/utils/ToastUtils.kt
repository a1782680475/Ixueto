package com.xktech.ixueto.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    var context: Context? = null
    fun showShort(errorMsg: String) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
    }
}