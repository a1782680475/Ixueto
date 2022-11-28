package com.xktech.ixueto.data.local

import com.chibatching.kotpref.KotprefModel
import com.xktech.ixueto.utils.Constants.DARKMODEL_FILE_NAME

object DarkModel : KotprefModel() {
    override val kotprefName: String = DARKMODEL_FILE_NAME
    var model by booleanPref()
    //自动规则是否启用
    var autoRuleEnable by booleanPref(true)
    //自动规则(0-跟随系统 1-定时开启)
    var autoRule by intPref(0)
    var darkStartTime by intPref(420)
    var darkEndTime by intPref(1200)
}