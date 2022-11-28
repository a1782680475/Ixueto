package com.xktech.ixueto.ui.face

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.vmadalin.easypermissions.EasyPermissions
import com.xktech.ixueto.MyApplication
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.ActivityFaceAuthBinding
import com.xktech.ixueto.model.CourseStudy
import com.xktech.ixueto.strategy.FaceCheckStrategy
import com.xktech.ixueto.strategy.ViolationCheckStrategy
import com.xktech.ixueto.utils.DimenUtils
import pub.devrel.easypermissions.AfterPermissionGranted
import java.util.*

class FaceAuthActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityFaceAuthBinding
    private lateinit var detectCloseView: ImageView
    private lateinit var cautionView: LinearLayout
    private lateinit var violateNumberView: TextView
    private var studyId: Int = 0
    private lateinit var courseStudy: CourseStudy
    private lateinit var stage: FaceCheckStrategy.Stage
    private var needLivenessCheck: Boolean = false
    private var violationCheckStrategyHashCode: Int = 0
    private var maxAllowCheckSeconds: Long? = null
    private var maxAllowViolatedNumber: Int? = null
    private var violateNumber: Int = 0
    private var violateCountDownMilliseconds: Long = 0
    private var facePageBright: Boolean = true
    private var violationCheckStrategy: ViolationCheckStrategy? = null
    private lateinit var startButton: Button
    private lateinit var subTitle: TextView
    private var isViewChange = false
    private lateinit var faceCheckActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        val dm: DisplayMetrics = resources.displayMetrics
        val whScale = dm.widthPixels.toDouble() / dm.heightPixels
        isViewChange = isChangeView(whScale)
        faceCheckActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    data?.apply {
                        val data = Intent()
                        data.putExtra("stage", stage)
                        data.putExtra("needLivenessCheck", needLivenessCheck)
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }
                }
            }
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun isChangeView(whScale: Double): Boolean {
        return whScale > 0.6f
    }

    private fun changeView() {
        val slp = (startButton.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomMargin = DimenUtils.dp2px(this@FaceAuthActivity, 40f).toInt()
        }
        startButton.layoutParams = slp
        val clp = (cautionView.layoutParams as LinearLayout.LayoutParams).apply {
            topMargin = DimenUtils.dp2px(this@FaceAuthActivity, 10f).toInt()
        }
        cautionView.layoutParams = clp
    }

    private fun initParam() {
        studyId = intent.getIntExtra("studyId", 0)
        courseStudy = intent.getSerializableExtra("courseStudy") as CourseStudy
        stage = intent.getSerializableExtra("stage") as FaceCheckStrategy.Stage
        needLivenessCheck = intent.getBooleanExtra("needLivenessCheck", true)
        violationCheckStrategyHashCode = intent.getIntExtra("violationCheckStrategyHashCode",0)
        maxAllowCheckSeconds = intent.getSerializableExtra("maxAllowCheckSeconds") as Long?
        maxAllowViolatedNumber = intent.getSerializableExtra("maxAllowViolatedNumber") as Int?
        violateNumber = intent.getIntExtra("violateNumber", 0)
        violateCountDownMilliseconds = intent.getLongExtra("violateCountDownMilliseconds", 0)
        facePageBright = intent.getBooleanExtra("facePageBright", true)
    }

    private fun initView() {
        binding = ActivityFaceAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        methodRequiresPermission()
        detectCloseView = binding.detectClose
        startButton = binding.start
        subTitle = binding.subTitle
        cautionView = binding.caution
        violateNumberView = binding.violateNumber
        if (isViewChange) {
            changeView()
        }
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        initParam()
        if (stage == FaceCheckStrategy.Stage.AFTER) {
            subTitle.visibility = View.VISIBLE
        } else {
            subTitle.visibility = View.GONE
        }
        startViolationCheckStrategy()
        detectCloseView.setOnClickListener {
            val data = Intent()
            setResult(Activity.RESULT_CANCELED, data)
            finish()
        }
        startButton.setOnClickListener {
            if (needLivenessCheck) {
                faceCheckActivityLauncher.launch(
                    Intent(
                        this,
                        FaceLivenessCheckActivity::class.java
                    )
                        .putExtra("studyId", studyId)
                        .putExtra("courseStudy", courseStudy)
                        .putExtra("stage", stage)
                        .putExtra("facePageBright", facePageBright)
                        .putExtra("needLivenessCheck", needLivenessCheck)
                )
            } else {
                faceCheckActivityLauncher.launch(
                    Intent(
                        this,
                        FaceDetectCheckActivity::class.java
                    )
                        .putExtra("studyId", studyId)
                        .putExtra("courseStudy", courseStudy)
                        .putExtra("stage", stage)
                        .putExtra("facePageBright", facePageBright)
                        .putExtra("needLivenessCheck", needLivenessCheck)
                )
            }
            //===============
//            val data = Intent()
//            data.putExtra("stage", stage)
//            data.putExtra("needLivenessCheck", needLivenessCheck)
//            setResult(Activity.RESULT_OK, data)
//            finish()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    private fun startViolationCheckStrategy() {
        //因为android 10及以上系统禁止从后台启动activity，所以在application中存入一个共享对象，在此activity弹出后在依据对象的内容修正违规判定
        val faceCheckOffsetTime =
            (application as MyApplication).faceCheckOffsetTime
        faceCheckOffsetTime?.maxAllowCheckSeconds?.let {
            if (faceCheckOffsetTime.violationCheckStrategyHashCode == violationCheckStrategyHashCode) {
                var localTimeStamp = Date().time
                //计算当前与开始人脸检测时的时间戳差值
                var surplusMS = localTimeStamp - faceCheckOffsetTime.startCheckTimeStamp
                violateCountDownMilliseconds += surplusMS
            }
        }
        initViolationCheckStrategy()
    }

    private fun initViolationCheckStrategy() {
        violationCheckStrategy =
            ViolationCheckStrategy(
                maxAllowCheckSeconds,
                maxAllowViolatedNumber,
                violateNumber
            )
        violationCheckStrategy?.currentCountDownMilliseconds = violateCountDownMilliseconds
        violationCheckStrategy?.setViolatedListener { violateNumber, isMax ->
            if (isMax) {
                resetStudy()
            } else {
                updateViolateCount(violateNumber)
            }
        }
        violationCheckStrategy?.startCheck()
    }

    private fun resetStudy() {
        binding.root.post {
            cautionView.visibility = View.VISIBLE
            violateNumberView.text = "您未按规定完成人脸识别，请重新学习"
            startButton.isEnabled = false
        }
    }

    private fun updateViolateCount(violateNumber: Int) {
        binding.root.post {
            cautionView.visibility = View.VISIBLE
            violateNumberView.text = "已违规${violateNumber}次，请及时完成人脸识别"
        }
    }

    @AfterPermissionGranted(2)
    private fun methodRequiresPermission() {
        if (!EasyPermissions.hasPermissions(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission),
                2,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onDestroy() {
        violationCheckStrategy?.cancel()
        super.onDestroy()
    }
}