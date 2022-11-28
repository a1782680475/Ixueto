package com.xktech.ixueto.data.local.serializer

import androidx.datastore.core.Serializer
import com.xktech.ixueto.datastore.StudyRule
import java.io.InputStream
import java.io.OutputStream

object StudyRuleSerializer : Serializer<StudyRule> {
    override val defaultValue: StudyRule= StudyRule.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): StudyRule {
        return StudyRule.parseFrom(input)
    }

    override suspend fun writeTo(t: StudyRule, output: OutputStream) {
        t.writeTo(output)
    }
}