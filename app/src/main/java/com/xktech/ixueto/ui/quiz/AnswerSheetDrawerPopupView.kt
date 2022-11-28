package com.xktech.ixueto.ui.quiz

import android.content.Context
import android.content.res.Configuration
import android.widget.ListView
import androidx.core.content.ContextCompat
import com.lxj.xpopup.core.DrawerPopupView
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.AnswerSheetAdapter
import com.xktech.ixueto.model.AnswerSheetItem

class AnswerSheetDrawerPopupView(
    context: Context,
) : DrawerPopupView(context) {
    private lateinit var questionListView: ListView
    private var onCreated: (() -> Unit)? = null
    lateinit var onSelectedListen: (Int, Int) -> Unit
    fun setCreatedListener(listener: () -> Unit) {
        this.onCreated = listener
    }
    var answerSheetData: MutableList<AnswerSheetItem> = mutableListOf()
    var currentQuestionIndex: Int = 0
    fun loadData() {
        var selectedColor = ContextCompat.getColor(context, R.color.md_theme_light_primary)
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            selectedColor = ContextCompat.getColor(context, R.color.md_theme_dark_primary)
        }
        questionListView.adapter =
            AnswerSheetAdapter(answerSheetData, currentQuestionIndex, selectedColor)
        questionListView.setSelection(currentQuestionIndex)
        questionListView.setOnItemClickListener { _, _, position, id ->
            onSelectedListen.invoke(position, id.toInt())
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.drawer_answer_sheet
    }

    override fun onCreate() {
        super.onCreate()
        questionListView = findViewById(R.id.question_list)
        onCreated?.let { it() }
    }

    fun setOnSelectedListener(listener: (Int, Int) -> Unit) {
        this.onSelectedListen = listener
    }
}