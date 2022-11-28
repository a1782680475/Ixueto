package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.service.RegionService
import com.xktech.ixueto.model.Region
import javax.inject.Inject

class RegionRepository @Inject constructor(var regionService: RegionService){
    suspend fun getRegions(): MutableList<Region> {
        val regionResponse = regionService.getRegions(mapOf("domain" to "www.ixueto.com"))
        return regionResponse.data!!
    }
}