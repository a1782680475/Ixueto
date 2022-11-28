package com.xktech.ixueto.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.xktech.ixueto.data.local.entity.CourseStudyDayData
import java.util.*

@Dao
interface CourseStudyDayDao {
    @Query("select * from course_study_day where user_id=:userId and subject_id=:subjectId and course_id=:courseId and date=:date")
    suspend fun getCourseStudyToday(
        userId: Int,
        subjectId: Int,
        courseId: Int,
        date: String
    ): CourseStudyDayData?

    @Insert
    suspend fun insertCourseStudy(courseStudyDayData: CourseStudyDayData)

    @Query("update course_study_day set studied_seconds=:studiedSeconds,update_time=:updateTime where id=:id")
    suspend fun updateStudiedSeconds(id: Int, studiedSeconds: Long, updateTime: Date)

    @Delete
    suspend fun deleteCourseStudyToday(courseStudyDayData: CourseStudyDayData)
}