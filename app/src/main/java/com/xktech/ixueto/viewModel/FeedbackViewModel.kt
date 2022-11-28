package com.xktech.ixueto.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.xktech.ixueto.data.remote.entity.request.RequestFeedbackAddInfo
import com.xktech.ixueto.model.DeviceInfo
import com.xktech.ixueto.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(private val feedbackRepository: FeedbackRepository) :
    ViewModel() {
    fun imageUpload(bitmap: Bitmap) = liveData(Dispatchers.IO) {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            emit(feedbackRepository.imageUpload(bytes))
        } catch (e: Exception) {

        }
    }

    fun addFeedback(
        title: String,
        content: String,
        imageList: MutableList<String>,
        deviceInfo: DeviceInfo?
    ) = liveData(Dispatchers.IO) {
        try {
            val addFeedbackInfo = RequestFeedbackAddInfo()
            addFeedbackInfo.title = title
            addFeedbackInfo.content = content
            addFeedbackInfo.imageList = imageList
            addFeedbackInfo.deviceInfo = deviceInfo
            emit(feedbackRepository.addFeedback(addFeedbackInfo))
        } catch (e: Exception) {

        }
    }
}