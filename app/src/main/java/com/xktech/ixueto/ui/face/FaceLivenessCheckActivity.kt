package com.xktech.ixueto.ui.face

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.baidu.idl.face.platform.FaceConfig
import com.baidu.idl.face.platform.FaceSDKManager
import com.baidu.idl.face.platform.FaceStatusNewEnum
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.baidu.idl.face.platform.listener.IInitCallback
import com.baidu.idl.face.platform.model.ImageInfo
import com.baidu.idl.face.platform.ui.FaceLivenessActivity
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.xktech.ixueto.data.remote.entity.request.FaceCheckInfo
import com.xktech.ixueto.model.CourseStudy
import com.xktech.ixueto.strategy.FaceCheckStrategy
import com.xktech.ixueto.viewModel.StudyViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class FaceLivenessCheckActivity : FaceLivenessActivity() {
    private var context: Context = this
    private var studyId: Int = 0
    private lateinit var courseStudy: CourseStudy
    private lateinit var stage: FaceCheckStrategy.Stage
    private var needLivenessCheck: Boolean = false
    private var facePageBright: Boolean = true
    private val studyViewModel: StudyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        initView()
        super.onCreate(savedInstanceState)
    }

    override fun onLivenessCompletion(
        status: FaceStatusNewEnum, message: String?,
        base64ImageCropMap: HashMap<String?, ImageInfo?>?,
        base64ImageSrcMap: HashMap<String?, ImageInfo?>?, currentLivenessCount: Int,
    ) {
        super.onLivenessCompletion(
            status,
            message,
            base64ImageCropMap,
            base64ImageSrcMap,
            currentLivenessCount
        )
        if (status == FaceStatusNewEnum.OK && mIsCompletion) {
            if (base64ImageSrcMap != null) {
                var base64 = getBestBase64Image(base64ImageSrcMap)
                faceCheck(base64!!)
            }
        } else if (status == FaceStatusNewEnum.DetectRemindCodeTimeout) {
            if (mViewBg != null) {
                mViewBg.visibility = View.VISIBLE
            }
        }
    }

    private fun getBestBase64Image(imageMap: HashMap<String?, ImageInfo?>?): String {
        val list: List<Map.Entry<String?, ImageInfo?>> = ArrayList(imageMap?.entries ?: null)
        Collections.sort(list) { p0, p1 ->
            val key1 = p0.key?.split("_")
            val score1 = key1?.get(2)
            val key2 = p1.key?.split("_")
            val score2 = key2?.get(2)
            score1?.let { score2?.toFloat()?.compareTo(it.toFloat()) } ?: 0
        }
        return list[0].value?.base64 ?: ""
    }

    private fun faceCheck(faceData: String) {
        mFaceDetectRoundView.setTipTopText("人脸对比中...")
        studyViewModel.faceCheck(
            FaceCheckInfo(
                studyId,
                courseStudy.courseInfo.StuSubId,
                courseStudy.courseInfo.SubjectId,
                courseStudy.courseInfo.ProfessionId,
                courseStudy.courseInfo.CourseId,
                courseStudy.courseInfo.GradeId,
                stage.ordinal,
                faceData
            )
        )
            .observe(this) { faceCheckResult ->
                if (faceCheckResult.Success) {
                    val data = Intent()
                    data.putExtra("stage", stage)
                    data.putExtra("needLivenessCheck", needLivenessCheck)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } else {
                    mFaceDetectRoundView.setTipTopText(faceCheckResult.ErrorMessage)
                    onPause()
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            mFaceConfig.livenessTypeList = getRandomLivenessType()
                            startPreview()
                        }
                    }, 2000)
                }
            }
    }

    fun initView() {
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        initParam()
        setFaceConfig()
        initLicense()
        if (facePageBright) {
            setScreenBright()
        }
    }

    private fun initParam() {
        studyId = intent.getIntExtra("studyId", 0)
        courseStudy = intent.getSerializableExtra("courseStudy") as CourseStudy
        stage = intent.getSerializableExtra("stage") as FaceCheckStrategy.Stage
        needLivenessCheck = intent.getBooleanExtra("needLivenessCheck", true)
        facePageBright = intent.getBooleanExtra("facePageBright", true)
    }

    private fun initLicense() {
        FaceSDKManager.getInstance().initialize(context, "xktech-ixueto-face-android",
            "idl-license.face-android", object : IInitCallback {
                override fun initSuccess() {
                    print("初始化成功")
                }

                override fun initFailure(errCode: Int, errMsg: String) {
                    print("初始化失败 = $errCode $errMsg")
                }
            })
    }

    private fun setFaceConfig() {
        mFaceConfig = FaceSDKManager.getInstance().faceConfig
        mFaceConfig.isLivenessRandom = false
        mFaceConfig.cacheImageNum = 3
        mFaceConfig.livenessRandomCount = 1
        mFaceConfig.livenessTypeList = getRandomLivenessType()
        mFaceConfig.isSound = false
    }

    private fun getRandomLivenessType(): List<LivenessTypeEnum> {
        var allLivenessTypes = mutableListOf(
            LivenessTypeEnum.Eye,
            LivenessTypeEnum.HeadDown,
            LivenessTypeEnum.HeadLeft,
            LivenessTypeEnum.HeadRight,
            LivenessTypeEnum.HeadUp,
            LivenessTypeEnum.Mouth
        )
        return FaceConfig.getRandomList(allLivenessTypes, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPreview()
    }
}