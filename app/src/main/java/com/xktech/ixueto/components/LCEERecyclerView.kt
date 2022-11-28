package com.xktech.ixueto.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.LceeRecyclerLayoutBinding
import com.xktech.ixueto.databinding.RecyclerEmptyLayoutBinding
import com.xktech.ixueto.databinding.RecyclerErrorLayoutBinding
import com.xktech.ixueto.databinding.RecyclerLoadingLayoutBinding

/**
 * Created by suson on 6/27/20
 * Custom recycler view with integrated error, empty and loading view
 */
class LCEERecyclerView constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val binding: LceeRecyclerLayoutBinding =
        LceeRecyclerLayoutBinding.inflate(LayoutInflater.from(context), this)
    private val emptyBinding: RecyclerEmptyLayoutBinding = binding.customEmptyView
    private val loadingBinding: RecyclerLoadingLayoutBinding = binding.customOverlayView
    private val errorBinding: RecyclerErrorLayoutBinding = binding.customErrorView
    fun setOnRetryListener(listener: () -> Unit) {
        this.onRetry = listener
    }
    private var onRetry: (() -> Unit)? = null

    val recyclerView: RecyclerView
        get() = binding.customRecyclerView
    var emptyText: String = ""
        set(value) {
            field = value
            emptyBinding.emptyMessage.text = value
        }
    private var scrollingEnabled: Boolean = true
        set(value) {
            field = value
            recyclerView.isNestedScrollingEnabled = value
        }

    @DrawableRes
    var emptyIcon = 0
        set(value) {
            field = value
            emptyBinding.emptyImage.setImageResource(value)
        }

    init {

        // inflate the layout
        this.minHeight = 300
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LCEERecyclerView,
            0,
            0
        ).apply {
            try {
                emptyText =
                    getString(R.styleable.LCEERecyclerView_emptyText) ?: "暂时没有内容"
                emptyIcon =
                    getResourceId(
                        R.styleable.LCEERecyclerView_emptyIcon,
                        R.drawable.ic_empty_image2
                    )
                scrollingEnabled = getBoolean(R.styleable.LCEERecyclerView_scrollingEnabled,true)
                errorBinding.retryButton.setOnClickListener {
                    onRetry?.let {
                        it()
                    }
                }
            } finally {
                recycle()
            }
        }
    }

    fun showEmptyView(msg: String? = null) {
        emptyText = msg ?: emptyText
        loadingBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.VISIBLE
    }

    fun showLoadingView() {
        emptyBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        loadingBinding.root.visibility = View.VISIBLE
    }

    fun showErrorView(){
        emptyBinding.root.visibility = View.GONE
        loadingBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.VISIBLE
    }

    fun hideAllViews() {
        loadingBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
    }

}