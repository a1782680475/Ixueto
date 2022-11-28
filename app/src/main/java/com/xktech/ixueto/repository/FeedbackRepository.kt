package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.RequestFeedbackAddInfo
import com.xktech.ixueto.data.remote.service.FeedbackService
import com.xktech.ixueto.model.SimpleResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class FeedbackRepository @Inject constructor(private val feedbackService: FeedbackService) {
    suspend fun imageUpload(bitArray: ByteArray): String {
        val body: RequestBody =  bitArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        var part = MultipartBody.Part.createFormData("file", "feedbackS.jpeg", body)
        val mulBody = MultipartBody.Builder().apply {
            addPart(part)
            setType("multipart/form-data".toMediaTypeOrNull()!!)
        }.build()
        return feedbackService.imageUpload(mulBody).data!!
    }
    suspend fun addFeedback(addFeedbackInfo: RequestFeedbackAddInfo): SimpleResult{
        return feedbackService.addFeedback(addFeedbackInfo).data!!
    }
}