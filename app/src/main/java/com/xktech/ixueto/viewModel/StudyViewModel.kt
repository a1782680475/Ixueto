package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.xktech.ixueto.data.local.StudyDatabase
import com.xktech.ixueto.data.local.entity.CourseStudyData
import com.xktech.ixueto.data.local.entity.CourseStudyDayData
import com.xktech.ixueto.data.remote.entity.request.FaceCheckInfo
import com.xktech.ixueto.data.remote.entity.request.ResetStudy
import com.xktech.ixueto.data.remote.entity.request.UpdateViolation
import com.xktech.ixueto.model.CourseStudy
import com.xktech.ixueto.model.FaceCheckTimeRuleEnum
import com.xktech.ixueto.model.LocalStudyData
import com.xktech.ixueto.model.SubjectStudyCheckBasis
import com.xktech.ixueto.repository.AuthenticationRepository
import com.xktech.ixueto.repository.RulePreferencesRepository
import com.xktech.ixueto.repository.StudyRepository
import com.xktech.ixueto.repository.UserInfoPreferencesRepository
import com.xktech.ixueto.utils.EnumUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val userInfoPreferencesRepository: UserInfoPreferencesRepository,
    private val studyRepository: StudyRepository,
    private val rulePreferencesRepository: RulePreferencesRepository,
    private val authenticationRepository: AuthenticationRepository,
    studyDatabase: StudyDatabase,
) : ViewModel() {
    private val courseStudyDao = studyDatabase.courseStudyDao()
    private val courseStudyDayDao = studyDatabase.courseStudyDayDao()
    fun getRuleFromWeb() = liveData(Dispatchers.IO) {
        try {
            val rule = studyRepository.getRule()
            emit(rule)
        } catch (e: Exception) {

        }
    }

    fun getRule() = liveData(Dispatchers.IO) {
        try {
            emit(rulePreferencesRepository.getRule())
        } catch (e: Exception) {

        }
    }


    fun setRule(rule: com.xktech.ixueto.model.Rule?) = liveData(Dispatchers.IO) {
        try {
            emit(rulePreferencesRepository.setRule(rule))
        } catch (e: Exception) {

        }
    }

    fun needOnceFaceCheck(
        subjectId: Int, courseId: Int, faceCheckTimeRule: Short,
    ) = liveData(Dispatchers.IO) {
        try {
            when (EnumUtils.getFaceCheckTimeRule(faceCheckTimeRule)) {
                FaceCheckTimeRuleEnum.FIXED_INTERVAL,
                FaceCheckTimeRuleEnum.RANDOM_INTERVAL,
                ->
                    emit(false)
                else ->
                    emit(studyRepository.needOnceFaceCheck(subjectId, courseId, faceCheckTimeRule))
            }
        } catch (e: Exception) {

        }
    }

    fun getCourseStudy(subjectId: Int, courseId: Int, stuId: Int?) = liveData(Dispatchers.IO) {
        try {
            val courseInfo =
                viewModelScope.async { studyRepository.getCourseInfo(subjectId, courseId) }
            val courseStudyInfo = viewModelScope.async { studyRepository.getCourseStudyInfo(stuId) }
            emit(CourseStudy(courseInfo.await(), courseStudyInfo.await()))
        } catch (e: Exception) {

        }
    }

    fun faceCheck(faceCheckInfo: FaceCheckInfo) = liveData(Dispatchers.IO) {
        try {
            emit(studyRepository.faceCheck(faceCheckInfo))
        } catch (e: Exception) {

        }
    }

    fun getSubjectStudyState() = liveData(Dispatchers.IO) {
        try {
            emit(studyRepository.getSubjectStudyState())
        } catch (e: Exception) {

        }
    }

    fun getSubjectStudyCheckBasis() = liveData(Dispatchers.IO) {
        try {
            val subjectStudyState = viewModelScope.async {
                studyRepository.getSubjectStudyState()
            }
            val authenticationState = viewModelScope.async {
                authenticationRepository.getAuthenticationState()
            }
            emit(
                SubjectStudyCheckBasis(
                    subjectStudyState.await(),
                    EnumUtils.getAuthenticationState(authenticationState.await())
                )
            )
        } catch (e: Exception) {

        }
    }

    fun getStudyCourseCount(subjectId: Int, type: Int) = liveData(Dispatchers.IO) {
        try {
            emit(studyRepository.getStudyCourseCount(subjectId, type))
        } catch (e: Exception) {

        }
    }

    fun getCourseStudyState(subjectId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(studyRepository.getCourseStudyState(subjectId))
        } catch (e: Exception) {

        }
    }

    fun initCourse(
        subjectId: Int,
        courseId: Int,
        studiedSeconds: Long?,
        studiedSecondsToday: Long?,
        lastStudyTimeStamp: Long?,
    ) = liveData(Dispatchers.IO) {
        try {
            emit(
                studyRepository.initCourse(
                    subjectId,
                    courseId,
                    studiedSeconds,
                    studiedSecondsToday,
                    lastStudyTimeStamp
                )
            )
        } catch (e: Exception) {

        }
    }

    fun saveStudy(
        subjectId: Int,
        professionId: Int,
        gradeId: Int,
        courseId: Int,
        studiedSeconds: Long?,
        studiedSecondsToday: Long?,
        studyDetailId: Int,
    ) = liveData(Dispatchers.IO) {
        try {
            emit(
                studyRepository.saveStudy(
                    subjectId,
                    professionId,
                    gradeId,
                    courseId,
                    studiedSeconds,
                    studiedSecondsToday,
                    studyDetailId
                )
            )
        } catch (e: Exception) {

        }
    }

    fun getCourseStudyInfo(stuId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(studyRepository.getCourseStudyInfo(stuId))
        } catch (e: Exception) {

        }
    }

    fun getStudiedSeconds(subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            val userId = userInfoPreferencesRepository.getUserInfo().userId
            val studiedSeconds = courseStudyDao.getStudiedSeconds(userId, subjectId, courseId)
            emit(studiedSeconds)
        } catch (e: Exception) {

        }
    }

    fun getCourseStudy(subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            val userId = userInfoPreferencesRepository.getUserInfo().userId
            emit(courseStudyDao.getCourseStudy(userId, subjectId, courseId))
        } catch (e: Exception) {

        }
    }

    fun getCourseStudyFromLocal(subjectId: Int, courseId: Int, needDayData: Boolean) =
        liveData(Dispatchers.IO) {
            try {
                val userId = userInfoPreferencesRepository.getUserInfo().userId
                val courseStudyData =
                    GlobalScope.async { courseStudyDao.getCourseStudy(userId, subjectId, courseId) }
                if (needDayData) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("CN"))
                    val nowTime = Date()
                    val dateString = sdf.format(nowTime)
                    val courseStudyTodayData = GlobalScope.async {
                        courseStudyDayDao.getCourseStudyToday(
                            userId,
                            subjectId,
                            courseId,
                            dateString
                        )
                    }
                    emit(
                        LocalStudyData(
                            courseStudyData.await(),
                            courseStudyTodayData.await()
                        )
                    )
                } else {
                    emit(
                        LocalStudyData(
                            courseStudyData.await(),
                            null
                        )
                    )
                }
            } catch (e: Exception) {

            }
        }

    fun clearCourseStudyFromLocal(
        courseStudy: CourseStudyData?,
        courseStudyDayData: CourseStudyDayData?
    ) = liveData(Dispatchers.IO) {
        try {
            courseStudy?.let {
                courseStudyDao.deleteCourseStudy(courseStudy)
            }
            courseStudyDayData?.let {
                courseStudyDayDao.deleteCourseStudyToday(courseStudyDayData)
            }
            emit(true)
        } catch (e: Exception) {

        }
    }

    fun getCourseStudyDay(subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            val userId = userInfoPreferencesRepository.getUserInfo().userId
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("CN"))
            val nowTime = Date()
            val dateString = sdf.format(nowTime)
            emit(courseStudyDayDao.getCourseStudyToday(userId, subjectId, courseId, dateString))
        } catch (e: Exception) {

        }
    }

    fun saveTodayStudiedSeconds(subjectId: Int, courseId: Int, studiedSeconds: Long) =
        liveData(Dispatchers.IO) {
            try {
                val userId = userInfoPreferencesRepository.getUserInfo().userId
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("CN"))
                val nowTime = Date()
                val dateString = sdf.format(nowTime)
                var courseStudyDayData =
                    courseStudyDayDao.getCourseStudyToday(userId, subjectId, courseId, dateString)
                if (courseStudyDayData == null) {
                    courseStudyDayData =
                        CourseStudyDayData(
                            0,
                            userId,
                            subjectId,
                            courseId,
                            studiedSeconds,
                            dateString,
                            nowTime,
                            nowTime
                        )
                    courseStudyDayDao.insertCourseStudy(courseStudyDayData)
                } else {
                    courseStudyDayDao.updateStudiedSeconds(
                        courseStudyDayData.id,
                        studiedSeconds,
                        nowTime
                    )
                }
                emit(true)
            } catch (e: Exception) {
                emit(false)
            }
        }

    fun saveStudiedSeconds(subjectId: Int, courseId: Int, studiedSeconds: Long) =
        liveData(Dispatchers.IO) {
            try {
                val userId = userInfoPreferencesRepository.getUserInfo().userId
                val nowTime = Date()
                var courseStudyData = courseStudyDao.getCourseStudy(userId, subjectId, courseId)
                if (courseStudyData == null) {
                    courseStudyData =
                        CourseStudyData(
                            0,
                            userId,
                            subjectId,
                            courseId,
                            studiedSeconds,
                            nowTime,
                            nowTime
                        )
                    courseStudyDao.insertCourseStudy(courseStudyData)
                } else {
                    courseStudyDao.updateStudiedSeconds(courseStudyData.id, studiedSeconds, nowTime)
                }
                emit(true)
            } catch (e: Exception) {
                emit(false)
            }
        }

    fun updateViolation(studyId: Int, subjectId: Int, courseId: Int, violateCount: Int) =
        liveData(Dispatchers.IO) {
            try {
                val success = studyRepository.updateViolation(
                    UpdateViolation(
                        studyId,
                        subjectId,
                        courseId,
                        violateCount
                    )
                )
                emit(success)
            } catch (e: Exception) {

            }
        }

    fun resetStudy(studyId: Int, subjectId: Int, courseId: Int) = liveData(Dispatchers.IO) {
        try {
            val success = studyRepository.resetStudy(ResetStudy(studyId, subjectId, courseId))
            emit(success)
        } catch (e: Exception) {

        }
    }

    fun getNextDayMilliseconds() = liveData(Dispatchers.IO) {
        try {
            var nextDayMilliseconds = 0L
            val serverTimeStamp = studyRepository.getTimeStamp()
            val nowTime = Date()
            var localTimeStamp = nowTime.time
            var nowTimeStamp = localTimeStamp
            if (abs(localTimeStamp - serverTimeStamp) > 10000) {
                nowTimeStamp = serverTimeStamp
            }
            val dayMilliSecond = 86400000
            val nextDayTimeStamp =
                nowTimeStamp - (nowTimeStamp + 28800000) % dayMilliSecond + dayMilliSecond
            nextDayMilliseconds = nextDayTimeStamp - nowTimeStamp
            emit(nextDayMilliseconds)
        } catch (e: Exception) {

        }
    }

    fun getTimeStamp() = liveData(Dispatchers.IO) {
        try {
            val serverTimeStamp = studyRepository.getTimeStamp()
            val nowTime = Date()
            var localTimeStamp = nowTime.time
            var nowTimeStamp = localTimeStamp
            var isLocalTime = true
            if (abs(localTimeStamp - serverTimeStamp) > 10000) {
                nowTimeStamp = serverTimeStamp
                isLocalTime = false
            }
            emit(TimeStampInfo(isLocalTime, nowTimeStamp))
        } catch (e: Exception) {

        }
    }

    fun getClassRuleAndTimeStamp(id: Int) = liveData(Dispatchers.IO) {
        try {
            val timeStampInfo = viewModelScope.async {
                val serverTimeStamp = studyRepository.getTimeStamp()
                val nowTime = Date()
                var localTimeStamp = nowTime.time
                var nowTimeStamp = localTimeStamp
                if (abs(localTimeStamp - serverTimeStamp) > 10000) {
                    nowTimeStamp = serverTimeStamp
                }
                nowTimeStamp
            }
            val classRule = viewModelScope.async { studyRepository.getClassRule(id) }
            emit(Pair(classRule.await(), timeStampInfo.await()))
        } catch (e: Exception) {

        }
    }

    data class TimeStampInfo(
        val isLocalTime: Boolean,
        val timeStamp: Long
    )
}