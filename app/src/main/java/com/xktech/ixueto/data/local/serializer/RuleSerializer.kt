package com.xktech.ixueto.data.local.serializer

import androidx.datastore.core.Serializer
import com.xktech.ixueto.datastore.Rule
import java.io.InputStream
import java.io.OutputStream

object RuleSerializer : Serializer<Rule> {
    override val defaultValue: Rule = Rule.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Rule {
        return Rule.parseFrom(input)
    }

    override suspend fun writeTo(t: Rule, output: OutputStream) {
        t.writeTo(output)
    }
}