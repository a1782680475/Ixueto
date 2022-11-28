package com.xktech.ixueto

import android.app.Application
import com.xktech.ixueto.model.FaceCheckOffsetTime
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    var faceCheckOffsetTime: FaceCheckOffsetTime? = null
}