package com.xktech.ixueto.data.local.serializer

import androidx.datastore.core.Serializer
import com.xktech.ixueto.datastore.Setting
import java.io.InputStream
import java.io.OutputStream

object SettingSerializer : Serializer<Setting> {
    override val defaultValue: Setting = Setting.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Setting {
        return Setting.parseFrom(input)
    }

    override suspend fun writeTo(t: Setting, output: OutputStream) {
        t.writeTo(output)
    }
}