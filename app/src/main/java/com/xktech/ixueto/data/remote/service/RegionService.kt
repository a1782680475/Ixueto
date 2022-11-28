package com.xktech.ixueto.data.remote.service

import com.xktech.ixueto.data.remote.entity.response.Response
import com.xktech.ixueto.model.Region
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface RegionService {
    @GET("region/getRegions")
    suspend fun getRegions(@QueryMap options: Map<String, String>): Response<MutableList<Region>>
}