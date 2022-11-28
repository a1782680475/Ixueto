package com.xktech.ixueto.data.local.serializer

import androidx.datastore.core.Serializer
import com.xktech.ixueto.datastore.UserInfo
import java.io.InputStream
import java.io.OutputStream

object UserInfoSerializer : Serializer<UserInfo> {
    override val defaultValue: UserInfo = UserInfo.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserInfo {
        return UserInfo.parseFrom(input)
    }

    override suspend fun writeTo(t: UserInfo, output: OutputStream) {
        t.writeTo(output)
    }
}