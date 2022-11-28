package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.model.Profession
import retrofit2.http.*

interface ProfessionService {
    @GET("profession/getPageProfession")
    suspend fun getPageProfession(
        @QueryMap options: Map<String, String>
    ): Response<ResponsePage<Profession>>
}