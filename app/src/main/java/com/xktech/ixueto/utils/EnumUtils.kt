package com.xktech.ixueto.utils

import com.xktech.ixueto.model.*

object EnumUtils {
    fun getStudyState(value: Short): StudyStateEnum {
        return when (value.toInt()) {
            0 -> StudyStateEnum.NOT_STARTED
            1 -> StudyStateEnum.STUDYING
            2 -> StudyStateEnum.FINISHED
            else -> StudyStateEnum.NOT_STARTED
        }
    }

    fun getSubjectStudyRule(value: Short): SubjectStudyRuleEnum {
        return when (value.toInt()) {
            0 -> SubjectStudyRuleEnum.UNLIMITED
            1 -> SubjectStudyRuleEnum.STUDY_FINISHED
            2 -> SubjectStudyRuleEnum.EXAM_QUALIFIED
            else -> SubjectStudyRuleEnum.UNLIMITED
        }
    }

    fun getAuthenticationState(value: Short): AuthenticationStateEnum {
        return when (value.toInt()) {
            0 -> AuthenticationStateEnum.UN_AUTHENTICATION
            1 -> AuthenticationStateEnum.AUTHENTICATED
            2 -> AuthenticationStateEnum.AUTHENTICATING
            3 -> AuthenticationStateEnum.AUTHENTICATE_FAILED
            4 -> AuthenticationStateEnum.AUTHENTICATE_FOR_ARTIFICIAL
            else -> AuthenticationStateEnum.UN_AUTHENTICATION
        }
    }

    fun getFaceCheckTimeRule(value: Short): FaceCheckTimeRuleEnum {
        return when (value.toInt()) {
            0 -> FaceCheckTimeRuleEnum.FIXED_INTERVAL
            1 -> FaceCheckTimeRuleEnum.RANDOM_INTERVAL
            2 -> FaceCheckTimeRuleEnum.ONE_EVERY_DAY
            3 -> FaceCheckTimeRuleEnum.ONE_EVERY_COURSE
            4 -> FaceCheckTimeRuleEnum.ONE_EVERY_SUBJECT
            else -> FaceCheckTimeRuleEnum.FIXED_INTERVAL
        }
    }

    fun getQuizPassRule(value: Short): QuizPassRuleEnum {
        return when (value.toInt()) {
            0 -> QuizPassRuleEnum.UNLIMITED
            1 -> QuizPassRuleEnum.QUALIFIED
            2 -> QuizPassRuleEnum.ALL_CORRECT
            else -> QuizPassRuleEnum.UNLIMITED
        }
    }

    fun getQuizStateEnum(value: Short): QuizStateEnum {
        return when (value.toInt()) {
            -1 -> QuizStateEnum.NOT_QUIZ
            0 -> QuizStateEnum.NOT_PASS
            1 -> QuizStateEnum.PASS
            2 -> QuizStateEnum.STUDY_RESET
            else -> QuizStateEnum.NOT_QUIZ
        }
    }

    fun getWithinStateEnum(value: Short): WithinStateEnum {
        return when (value.toInt()) {
            0 -> WithinStateEnum.DATE_LIMITED
            1 -> WithinStateEnum.UNLIMITED
            2 -> WithinStateEnum.TIME_OF_DAY_LIMITED
            3 -> WithinStateEnum.DATE_AND_TIME_LIMITED
            else -> WithinStateEnum.DATE_LIMITED
        }
    }

    fun getPhotoTypeEnum(value: Short): PhotoTypeEnum {
        return when (value.toInt()) {
            0 -> PhotoTypeEnum.ID_CARD_FRONT
            1 -> PhotoTypeEnum.ID_CARD_BACK
            2 -> PhotoTypeEnum.RECENT
            3 -> PhotoTypeEnum.STUDENT_STATUS
            4 -> PhotoTypeEnum.RESIDENCE_INDEX
            5 -> PhotoTypeEnum.RESIDENCE_DETAIL
            6 -> PhotoTypeEnum.DIPLOMA
            7 -> PhotoTypeEnum.STANDARD
            else -> PhotoTypeEnum.ID_CARD_FRONT
        }
    }
}