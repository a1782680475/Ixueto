package com.xktech.ixueto.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "course_study",
    indices = [Index(value = ["user_id", "subject_id", "course_id"])])
data class CourseStudyData(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "user_id") var userId: Int,
    @ColumnInfo(name = "subject_id") var subjectId: Int,
    @ColumnInfo(name = "course_id") var courseId: Int,
    @ColumnInfo(name = "studied_seconds") var studiedSeconds: Long,
    @ColumnInfo(name = "create_time") var createTime: Date,
    @ColumnInfo(name = "update_time") var updateTime: Date,
)
