package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.request.RequestFeedbackAddInfo
import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.model.SimpleResult
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackService {
    @POST("feedback/imageUpload")
    suspend fun imageUpload(@Body body: RequestBody): Response<String>
    @POST("feedback/addFeedback")
    suspend fun addFeedback(@Body addFeedbackInfo: RequestFeedbackAddInfo): Response<SimpleResult>
}