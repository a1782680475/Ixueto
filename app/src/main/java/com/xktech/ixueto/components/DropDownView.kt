package com.xktech.ixueto.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R
import com.xktech.ixueto.utils.DimenUtils

class DropDownView(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private var onSelectedClick: ((Int) -> Unit)? = null
    private val items = arrayOf("全部课程", "未完成", "已完成")
    private var titleView: TextView
    private val listPopupWindow: ListPopupWindow
    fun setOnSelectedClickListener(listener: (Int) -> Unit) {
        this.onSelectedClick = listener
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.drop_down_view, this, true)
        var shortPadding = DimenUtils.dp2px(context, 8f).toInt()
        var longPadding = DimenUtils.dp2px(context, 12f).toInt()
        setPadding(longPadding, shortPadding, longPadding, shortPadding)
        background = ContextCompat.getDrawable(context, R.drawable.drop_down)
        gravity = Gravity.CENTER
        listPopupWindow = ListPopupWindow(
            context!!,
            null,
            com.google.android.material.R.attr.listPopupWindowStyle
        )
        listPopupWindow.isModal = true
        listPopupWindow.anchorView = this
        listPopupWindow.verticalOffset = DimenUtils.dp2px(context, 5f).toInt()
        val adapter = ArrayAdapter(context, R.layout.item_spinner, items)
        listPopupWindow.setAdapter(adapter)
        titleView = findViewById<TextView>(R.id.title)
        listPopupWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            listPopupWindow.dismiss()
            titleView.text = items[position]
            onSelectedClick?.let { it(position) }
        }
        listPopupWindow.setOnDismissListener {
            listPopupWindow.dismiss()
        }
        this.setOnClickListener {
            if (!listPopupWindow.isShowing) {
                showDropDown()
            }
        }
    }

    private fun showDropDown() {
        listPopupWindow.show()
    }

    fun dismissDropDown() {
        listPopupWindow.dismiss()
    }

    fun setValue(index: Int) {
        titleView.text = items[index]
    }
}