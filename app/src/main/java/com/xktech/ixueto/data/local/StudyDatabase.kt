package com.xktech.ixueto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xktech.ixueto.data.local.converter.DateConverter
import com.xktech.ixueto.data.local.dao.CourseStudyDao
import com.xktech.ixueto.data.local.dao.CourseStudyDayDao
import com.xktech.ixueto.data.local.entity.CourseStudyData
import com.xktech.ixueto.data.local.entity.CourseStudyDayData

@Database(version = 1, entities = [CourseStudyData::class, CourseStudyDayData::class])
@TypeConverters(DateConverter::class)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun courseStudyDao(): CourseStudyDao
    abstract fun courseStudyDayDao(): CourseStudyDayDao
}