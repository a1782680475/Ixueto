package com.xktech.ixueto.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.xktech.ixueto.data.local.entity.CourseStudyData
import java.util.*

@Dao
interface CourseStudyDao {

    @Query("select * from course_study where user_id=:userId and subject_id=:subjectId and course_id=:courseId")
    suspend fun getCourseStudy(userId: Int, subjectId: Int, courseId: Int): CourseStudyData?

    @Query("select studied_seconds from course_study where user_id=:userId and subject_id=:subjectId and course_id=:courseId")
    suspend fun getStudiedSeconds(userId: Int, subjectId: Int, courseId: Int): Long?

    @Insert
    suspend fun insertCourseStudy(courseStudyData: CourseStudyData)

    @Query("update course_study set studied_seconds=:studiedSeconds,update_time=:updateTime where id=:id")
    suspend fun updateStudiedSeconds(id: Int, studiedSeconds: Long, updateTime: Date)

    @Delete
    suspend fun deleteCourseStudy(courseStudy: CourseStudyData)
}