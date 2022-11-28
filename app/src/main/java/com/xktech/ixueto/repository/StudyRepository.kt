package com.xktech.ixueto.repository

import com.xktech.ixueto.data.remote.entity.request.*
import com.xktech.ixueto.data.remote.entity.request.CourseStudy
import com.xktech.ixueto.data.remote.service.StudyService
import com.xktech.ixueto.model.*
import javax.inject.Inject

class StudyRepository @Inject constructor(private val studyService: StudyService) {

    suspend fun getStudyCourseCount(subjectId: Int, type: Int): Int {
        return studyService.getStudyCourseCount(subjectId, type).data!!
    }

    suspend fun getCourseInfo(subjectId: Int, courseId: Int): CourseInfo {
        return studyService
            .getCourseInfo(subjectId, courseId).data!!
    }

    suspend fun getRule(): Rule {
        return studyService.getRule().data!!
    }

    suspend fun needOnceFaceCheck(
        subjectId: Int, courseId: Int, faceCheckTimeRule: Short,
    ): Boolean {
        return studyService.needOnceFaceCheck(subjectId, courseId, faceCheckTimeRule).data!!
    }

    suspend fun faceCheck(faceCheckInfo: FaceCheckInfo): FaceCheckResult {
        return studyService.faceCheck(faceCheckInfo).data!!
    }

    suspend fun getSubjectStudyState(): SubjectStudyState {
        return studyService.getSubjectStudyState().data!!
    }

    suspend fun getCourseStudyState(subjectId: Int): CourseStudyState {
        return studyService.getCourseStudyState(subjectId).data!!
    }

    suspend fun initCourse(
        subjectId: Int,
        courseId: Int,
        studiedSeconds: Long?,
        studiedSecondsToday: Long?,
        lastStudyTimeStamp: Long?,
    ): CourseInitResult {
        return studyService.initCourse(
            CourseInit(
                subjectId,
                courseId,
                studiedSeconds,
                studiedSecondsToday,
                lastStudyTimeStamp
            )
        ).data!!
    }

    suspend fun saveStudy(
        subjectId: Int,
        professionId: Int,
        gradeId: Int,
        courseId: Int,
        studiedSeconds: Long?,
        studiedSecondsToday: Long?,
        studyDetailId: Int,
    ): Boolean {
        return studyService.saveStudy(
            CourseStudy(
                subjectId,
                professionId,
                gradeId,
                courseId,
                studiedSeconds,
                studiedSecondsToday,
                studyDetailId
            )
        ).data!!
    }

    suspend fun getCourseStudyInfo(stuId: Int): CourseStudyInfo {
        return studyService.getCourseStudyInfo(stuId).data!!
    }

    suspend fun updateViolation(updateViolation: UpdateViolation): Boolean {
        return studyService.updateViolation(updateViolation).data!!
    }

    suspend fun resetStudy(resetStudy: ResetStudy): Boolean {
        return studyService.resetStudy(resetStudy).data!!
    }

    suspend fun getTimeStamp(): Long {
        return studyService.getTimeStamp().data!!
    }

    suspend fun getClassRule(id: Int): ClassRule? {
        return studyService.getClassRule(id).data!!
    }
}