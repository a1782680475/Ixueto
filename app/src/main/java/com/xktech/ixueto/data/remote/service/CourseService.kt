package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.model.Course
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CourseService {
    @GET("course/getPageCourse")
    suspend fun getPageCourse(
        @QueryMap options: Map<String, String>
    ): Response<ResponsePage<Course>>
}