package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.service.SubjectService
import com.xktech.ixueto.model.Subject
import java.lang.Exception
import javax.inject.Inject

class SubjectRepository @Inject constructor(var subjectService: SubjectService) {
    suspend fun getRegions(professionId: Int): MutableList<Subject> {
        return try {
            val regionResponse =
                subjectService.getSubjects(professionId)
            regionResponse.data!!
        } catch (e: Exception) {
            return mutableListOf()
        }
    }

    suspend fun signUp(subjectId: Int): SignUpResult {
        val result = subjectService.signUp(subjectId)
        return SignUpResult(result.data!!, result.msg)
    }

    data class SignUpResult(val result: Boolean, val message: String)
}