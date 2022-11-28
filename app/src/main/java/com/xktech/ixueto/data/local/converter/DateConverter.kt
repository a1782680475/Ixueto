package com.xktech.ixueto.data.local.converter

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

object DateConverter {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @TypeConverter
    fun revertDate(value: String): Date {
        return sdf.parse(value)
    }

    @TypeConverter
    fun converterDate(value: Date): String {
        return sdf.format(value)
    }

    @TypeConverter
    fun converterDate(value: Long): String {
        return sdf.format(Date(value))
    }

    fun converterDate(value: Long, format: String): String {
        return SimpleDateFormat(format).format(Date(value))
    }
}