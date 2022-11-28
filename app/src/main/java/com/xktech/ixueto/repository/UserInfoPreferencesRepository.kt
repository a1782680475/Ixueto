package com.xktech.ixueto.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.xktech.ixueto.data.local.serializer.UserInfoSerializer
import com.xktech.ixueto.datastore.UserInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class UserInfoPreferencesRepository @Inject constructor(
    private val context: Context,
) {
    private val Context.userInfoPreferencesStore: DataStore<UserInfo> by dataStore(
        fileName = "userInfo.pb",
        serializer = UserInfoSerializer
    )

    fun getUserInfoSync(): UserInfo {
        return runBlocking{
             context.userInfoPreferencesStore.data.first()
        }
    }

    suspend fun getUserInfo(): UserInfo {
        return context.userInfoPreferencesStore.data.first()
    }

    suspend fun setUserInfoSync(userInfo: com.xktech.ixueto.model.UserInfo?) {
        return runBlocking {
            if (userInfo != null) {
                context.userInfoPreferencesStore.updateData { currentRule ->
                    currentRule.toBuilder()
                        .setToken(userInfo.Token)
                        .setWebToken(userInfo.WebToken)
                        .setDesUsername(userInfo.DesUsername)
                        .setDesUserType(userInfo.DesUserType)
                        .setUserId(userInfo.UserId)
                        .setUsername(userInfo.UserName)
                        .setName(userInfo.Name)
                        .setPhone(userInfo.Phone)
                        .setAvatar(userInfo.Avatar)
                        .build()
                }
            } else {
                context.userInfoPreferencesStore.updateData { currentRule ->
                    currentRule.toBuilder().clear().build()
                }
            }
        }
    }
}