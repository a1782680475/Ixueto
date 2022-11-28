package com.xktech.ixueto.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.xktech.ixueto.R

class UserCenterOperationItem(context: Context, attributeSet: AttributeSet) : LinearLayout(context,attributeSet) {
    init {
        val inflate = LayoutInflater.from(context).inflate(R.layout.user_center_operation_item, this,true)
        val icon = inflate.findViewById<ImageView>(R.id.userCenter_operation_item_icon)
        val text = inflate.findViewById<TextView>(R.id.userCenter_operation_item_text)
        val attr = context.obtainStyledAttributes(attributeSet, R.styleable.UserCenterOperationItem)
        icon.setImageDrawable(attr.getDrawable(R.styleable.UserCenterOperationItem_icon_src))
        text.text = attr.getString(R.styleable.UserCenterOperationItem_text)
        attr.recycle()
    }
}