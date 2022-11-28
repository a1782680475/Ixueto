package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.model.Subject
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SubjectService {
    @GET("subject/getSubjects")
    suspend fun getSubjects(@Query("professionId") professionId: Int):  Response<MutableList<Subject>>
    @POST("subject/signUp/{id}")
    suspend fun signUp(@Path("id") id: Int): Response<Boolean>
}