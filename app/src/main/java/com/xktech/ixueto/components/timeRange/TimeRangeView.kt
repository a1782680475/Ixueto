package com.xktech.ixueto.components.timeRange

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.xktech.ixueto.R

class TimeRangeView(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {
    private var isNightMode = false
    private var titleView: TextView
    private var startDateView: TextView
    private var endDateView: TextView
    private var startTimeView: TextView
    private var endTimeView: TextView
    private var labelDailyView: View
    private var dailyTitleView: View
    private var dailyTitleLineView: View
    private var dailyTimeView: TextView
    private var labelStartView: RoundedRectangleTextView
    private var labelEndView: RoundedRectangleTextView
    private var lineView: LineView
    var timeRangeSetting = TimeRangeSetting()

    init {
        val rootView = LayoutInflater.from(context).inflate(R.layout.layout_time_range, this, true)
        titleView = rootView.findViewById(R.id.title)
        startDateView = rootView.findViewById(R.id.start_date)
        endDateView = rootView.findViewById(R.id.end_date)
        startTimeView = rootView.findViewById(R.id.start_time)
        endTimeView = rootView.findViewById(R.id.end_time)
        labelDailyView = rootView.findViewById(R.id.label_daily)
        dailyTitleView = rootView.findViewById(R.id.daily_title)
        dailyTitleLineView = rootView.findViewById(R.id.daily_title_line)
        dailyTimeView = rootView.findViewById(R.id.daily_time)
        labelStartView = rootView.findViewById(R.id.label_start)
        labelEndView = rootView.findViewById(R.id.label_end)
        lineView = rootView.findViewById(R.id.line)
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true
        }
        val attr = context?.obtainStyledAttributes(attrs, R.styleable.TimeRangeView)
        var defaultPrimaryColorId: Int = R.color.md_theme_light_primary
        if (isNightMode) {
            defaultPrimaryColorId = R.color.md_theme_dark_primary
        }
        var defaultPrimaryColor = ContextCompat.getColor(context, defaultPrimaryColorId)
        var primaryColor: Int
        attr?.let {
            primaryColor =
                attr.getColor(R.styleable.TimeRangeView_primaryColor, defaultPrimaryColor)
            var titleStr = attr.getString(R.styleable.TimeRangeView_title)
            var startDateStr = attr.getString(R.styleable.TimeRangeView_startDate)
            var startTimeStr = attr.getString(R.styleable.TimeRangeView_startTime)
            var endDateStr = attr.getString(R.styleable.TimeRangeView_endDate)
            var endTimeStr = attr.getString(R.styleable.TimeRangeView_endTime)
            var isDailyShow = attr.getBoolean(R.styleable.TimeRangeView_isDailyShow, false)
            var dailyStartTimeStr = attr.getString(R.styleable.TimeRangeView_dailyStartTime)
            var dailyEndTimeStr = attr.getString(R.styleable.TimeRangeView_dailyEndTime)
            timeRangeSetting.primaryColor = primaryColor
            timeRangeSetting.title = titleStr
            timeRangeSetting.startDate = startDateStr
            timeRangeSetting.startTime = startTimeStr
            timeRangeSetting.endDate = endDateStr
            timeRangeSetting.endTime = endTimeStr
            timeRangeSetting.isDailyShow = isDailyShow
            timeRangeSetting.dailyStartTime = dailyStartTimeStr
            timeRangeSetting.dailyEndTime = dailyEndTimeStr
            updateView()
        }
    }

    fun updateView() {
        setInfo(timeRangeSetting)
    }

    private fun setInfo(
        settingData: TimeRangeSetting
    ) {
        var isShow = true
        if (settingData.startDate == null && settingData.endDate == null) {
            if (settingData.isDailyShow) {
                if (settingData.dailyStartTime == null && settingData.dailyEndTime == null){
                    isShow = false
                }
            } else {
                isShow = false
            }
        }
        if(isShow) {
            var defaultStr = "未设置"
            val titleValue = if (settingData.title.isNullOrEmpty()) {
                defaultStr
            } else {
                settingData.title
            }
            val startDateValue = if (settingData.startDate.isNullOrEmpty()) {
                defaultStr
            } else {
                settingData.startDate
            }
            val startTimeValue =
                if (settingData.startDate.isNullOrEmpty()) {
                    null
                } else if (settingData.startTime.isNullOrEmpty()) {
                    defaultStr
                } else {
                    settingData.startTime
                }
            val endDateValue = if (settingData.endDate.isNullOrEmpty()) {
                defaultStr
            } else {
                settingData.startDate
            }
            val endTimeValue = if (settingData.endDate.isNullOrEmpty()) {
                null
            } else if (settingData.endTime.isNullOrEmpty()) {
                defaultStr
            } else {
                settingData.endTime
            }
            val dailyStartTimeValue = if (settingData.dailyStartTime.isNullOrEmpty()) {
                defaultStr
            } else {
                settingData.dailyStartTime
            }
            val dailyEndTimeValue = if (settingData.dailyEndTime.isNullOrEmpty()) {
                defaultStr
            } else {
                settingData.dailyEndTime
            }
            var dailyTimeValue =
                if (settingData.dailyStartTime.isNullOrEmpty() && settingData.dailyEndTime.isNullOrEmpty()) {
                    settingData.isDailyShow = false
                    ""
                } else {
                    "${dailyStartTimeValue}-${dailyEndTimeValue}"
                }
            titleView.setBackgroundColor(settingData.primaryColor)
            labelStartView.setBackgroundColor(settingData.primaryColor)
            labelEndView.setBackgroundColor(settingData.primaryColor)
            lineView.setLineColor(settingData.primaryColor)
            startDateView.setTextColor(settingData.primaryColor)
            startTimeView.setTextColor(settingData.primaryColor)
            endDateView.setTextColor(settingData.primaryColor)
            endTimeView.setTextColor(settingData.primaryColor)
            titleView.text = titleValue
            startDateView.text = startDateValue
            if (startTimeValue == null) {
                startTimeView.visibility = View.GONE
            } else {
                startTimeView.text = startTimeValue
            }
            endDateView.text = endDateValue
            if (endTimeValue == null) {
                endTimeView.visibility = View.GONE
            } else {
                endTimeView.text = endTimeValue
            }
            var dailyVisibility = View.VISIBLE
            if (!settingData.isDailyShow) {
                dailyVisibility = View.GONE
            } else {
                dailyTimeView.text = dailyTimeValue
            }
            labelDailyView.visibility = dailyVisibility
            dailyTitleView.visibility = dailyVisibility
            dailyTitleLineView.visibility = dailyVisibility
            dailyTimeView.visibility = dailyVisibility
            this.visibility = VISIBLE
        }else{
            this.visibility = GONE
        }
    }

    class TimeRangeSetting {
        var primaryColor: Int = 0
        var title: String? = null
        var startDate: String? = null
        var startTime: String? = null
        var endDate: String? = null
        var endTime: String? = null
        var isDailyShow: Boolean = false
        var dailyStartTime: String? = null
        var dailyEndTime: String? = null
    }
}