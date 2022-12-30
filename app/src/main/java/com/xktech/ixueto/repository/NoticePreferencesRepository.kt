package com.xktech.ixueto.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.xktech.ixueto.data.local.serializer.NoticeSerializer
import com.xktech.ixueto.datastore.Notice
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NoticePreferencesRepository @Inject constructor(
    private val context: Context,
) {
    private val Context.noticePreferencesStore: DataStore<Notice> by dataStore(
        fileName = "notice.pb",
        serializer = NoticeSerializer
    )

    suspend fun getNotice(): Notice {
        return context.noticePreferencesStore.data.first()
    }

    suspend fun read() {
        context.noticePreferencesStore.updateData { data ->
            data.toBuilder()
                .setIsRead(
                   true
                )
                .build()
        }
    }
}