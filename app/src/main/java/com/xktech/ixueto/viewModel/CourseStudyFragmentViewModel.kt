package com.xktech.ixueto.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xktech.ixueto.model.QuizStateEnum
import com.xktech.ixueto.model.StudyStateEnum
import com.xktech.ixueto.model.StudyStepEnum


class CourseStudyFragmentViewModel : ViewModel() {
    val studiedSeconds: MutableLiveData<Long> = MutableLiveData()
    val studyProgress: MutableLiveData<Int> = MutableLiveData()
    //注意，此处学习状态与CourseStudyFragment中的studyState变量意义不一样，此处仅指学习结束（含尾部人脸识别判定，但是不包含单元测试判定）
    val studyState: MutableLiveData<StudyStateEnum> = MutableLiveData()
    //当前所属学习阶段
    val studyStep: MutableLiveData<StudyStepEnum> = MutableLiveData()
    val quizState: MutableLiveData<QuizStateEnum?> = MutableLiveData()
}