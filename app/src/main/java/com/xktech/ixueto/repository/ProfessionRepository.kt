package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.RequestProfession
import com.xktech.ixueto.data.remote.entity.response.ResponsePage
import com.xktech.ixueto.data.remote.service.ProfessionService
import com.xktech.ixueto.model.Profession
import java.lang.Exception
import javax.inject.Inject

class ProfessionRepository @Inject constructor(private var professionService: ProfessionService) {
    suspend fun getData(request: RequestProfession): ResponsePage<Profession> {
        var map = mapOf(
            "domain" to request.domain,
            "regionId" to request.regionId.toString(),
            "professionName" to request.professionName,
            "page" to request.page.toString(),
            "pageSize" to request.pageSize.toString(),
        )
        return professionService
            .getPageProfession(map).data!!
    }
}