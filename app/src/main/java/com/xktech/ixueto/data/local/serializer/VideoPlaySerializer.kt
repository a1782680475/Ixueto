package com.xktech.ixueto.data.local.serializer

import androidx.datastore.core.Serializer
import com.xktech.ixueto.datastore.VideoPlay
import java.io.InputStream
import java.io.OutputStream

object VideoPlaySerializer : Serializer<VideoPlay> {
    override val defaultValue: VideoPlay = VideoPlay.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): VideoPlay {
        return VideoPlay.parseFrom(input)
    }

    override suspend fun writeTo(t: VideoPlay, output: OutputStream) {
        t.writeTo(output)
    }
}