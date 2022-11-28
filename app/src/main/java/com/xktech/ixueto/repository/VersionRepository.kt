package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.service.VersionService
import com.xktech.ixueto.model.Version
import javax.inject.Inject

class VersionRepository @Inject constructor(var versionService: VersionService){
    suspend fun versionCheck(): Version {
        val regionResponse = versionService.versionCheck()
        return regionResponse.data!!
    }
}