package com.xktech.ixueto.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.xktech.ixueto.data.local.serializer.SettingSerializer
import com.xktech.ixueto.datastore.Setting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SettingPreferencesRepository @Inject constructor(
    private val context: Context,
) {
    private val Context.settingPreferencesStore: DataStore<Setting> by dataStore(
        fileName = "setting.pb",
        serializer = SettingSerializer
    )

    suspend fun getSetting(): Setting {
        return context.settingPreferencesStore.data.first()
    }

    suspend fun setAlertAtNotWifi(enable: Boolean) {
        context.settingPreferencesStore.updateData { data ->
            var gesture: Boolean = if (!data.videoPlay.hasGesture()) {
                true
            } else {
                data.videoPlay.gesture
            }
            data.toBuilder()
                .setVideoPlay(
                    data.videoPlay.toBuilder().setAlertAtNotWifi(enable)
                        .setGesture(gesture).build()
                )
                .build()
        }
    }

    suspend fun setGesture(enable: Boolean) {
        context.settingPreferencesStore.updateData { data ->
            var alertAtNotWifi: Boolean = if (!data.videoPlay.hasAlertAtNotWifi()) {
                true
            } else {
                data.videoPlay.alertAtNotWifi
            }
            data.toBuilder()
                .setVideoPlay(
                    data.videoPlay.toBuilder().setGesture(enable)
                        .setAlertAtNotWifi(alertAtNotWifi).build()
                )
                .build()
        }
    }

    suspend fun setStartupPage(index: Int) {
        context.settingPreferencesStore.updateData { data ->
            data.toBuilder().setStartupPage(index).build()
        }
    }

    fun getSettingSync(): Setting {
        return runBlocking {
            context.settingPreferencesStore.data.first()
        }
    }


    suspend fun setFacePageBright(enable: Boolean) {
        context.settingPreferencesStore.updateData { data ->
            data.toBuilder().setFacePageBright(enable).build()
        }
    }

}