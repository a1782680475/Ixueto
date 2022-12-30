package com.xktech.ixueto.data.local.serializer

import androidx.datastore.core.Serializer
import com.xktech.ixueto.datastore.Notice
import java.io.InputStream
import java.io.OutputStream

object NoticeSerializer : Serializer<Notice> {
    override val defaultValue: Notice = Notice.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Notice {
        return Notice.parseFrom(input)
    }

    override suspend fun writeTo(t: Notice, output: OutputStream) {
        t.writeTo(output)
    }
}