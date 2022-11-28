package com.xktech.ixueto.data.remote.entity.request

import com.xktech.ixueto.model.DeviceInfo

class RequestFeedbackAddInfo {
    var title: String = ""
    var content: String = ""
    var imageList: MutableList<String> = mutableListOf()
    var deviceInfo: DeviceInfo? = null
}