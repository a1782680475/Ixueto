package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.RequestCourse
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.data.remote.service.CourseService
import com.xktech.ixueto.model.Course
import javax.inject.Inject

class CourseRepository @Inject constructor(private var courseService: CourseService) {
    suspend fun getData(request: RequestCourse): ResponsePage<Course> {
        var map = mapOf(
            "subjectId" to request.subjectId.toString(),
            "page" to request.page.toString(),
            "pageSize" to request.pageSize.toString(),
        )
        return courseService
            .getPageCourse(map).data!!
    }
}