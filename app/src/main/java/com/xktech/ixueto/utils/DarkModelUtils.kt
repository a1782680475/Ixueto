package com.xktech.ixueto.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.xktech.ixueto.data.local.DarkModel
import java.text.SimpleDateFormat

object DarkModelUtils {
    fun check(context: Context): Boolean {
        var isDark = false
        if (!DarkModel.autoRuleEnable) {
            isDark = DarkModel.model
            if(isDark){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            when (DarkModel.autoRule) {
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    isDark =
                        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                    DarkModel.model = isDark
                }
                1 -> {
                    val now = System.currentTimeMillis()
                    val sdfOne = SimpleDateFormat("yyyy-MM-dd")
                    val minutes: Long = (now - sdfOne.parse(sdfOne.format(now)).time) / 60000
                    //深色模式不跨天
                    if (DarkModel.darkEndTime >= DarkModel.darkStartTime) {
                        if (minutes >= DarkModel.darkStartTime && minutes < DarkModel.darkEndTime) {
                            DarkModel.model = true
                            isDark = true
                        } else {
                            DarkModel.model = false
                            isDark = false
                        }
                    } else {
                        if (minutes >= DarkModel.darkStartTime || minutes < DarkModel.darkEndTime) {
                            DarkModel.model = true
                            isDark = true
                        } else {
                            DarkModel.model = false
                            isDark = false
                        }
                    }
                }
            }
            if(DarkModel.autoRule!=0) {
                if (isDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
        return isDark
    }
}