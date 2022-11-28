package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.model.Version
import retrofit2.http.GET

interface VersionService {
    @GET("version/check")
    suspend fun versionCheck(): Response<Version>
}