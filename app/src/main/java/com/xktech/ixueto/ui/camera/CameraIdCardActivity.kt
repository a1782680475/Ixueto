package com.xktech.ixueto.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.google.common.util.concurrent.ListenableFuture
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.vmadalin.easypermissions.EasyPermissions
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.ActivityCameraIdCardBinding
import com.xktech.ixueto.model.PhotoTypeEnum
import com.xktech.ixueto.utils.DimenUtils
import com.xktech.ixueto.utils.EnumUtils
import pub.devrel.easypermissions.AfterPermissionGranted
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraIdCardActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private var binding: ActivityCameraIdCardBinding? = null
    private lateinit var root: ViewGroup
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var cropView: ShapeableImageView
    private lateinit var closeButton: ImageView
    private lateinit var cropLeftView: View
    private lateinit var cropRightView: View
    private lateinit var cropTopView: View
    private lateinit var cropBottomView: View
    private lateinit var tool: LinearLayout
    private lateinit var submitContainer: LinearLayout
    private lateinit var cancelButton: ImageView
    private lateinit var submitButton: ImageView
    private lateinit var cameraExecutor: ExecutorService
    private var orientationEventListener: OrientationEventListener? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var photoType: PhotoTypeEnum = PhotoTypeEnum.ID_CARD_BACK
    private var photo: Bitmap? = null
    private var userId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        binding = ActivityCameraIdCardBinding.inflate(layoutInflater)
        root = binding!!.root
        previewView = binding!!.preview
        captureButton = binding!!.captureButton
        closeButton = binding!!.closeButton
        cropView = binding!!.cropView
        cropLeftView = binding!!.cropLeft
        cropRightView = binding!!.cropRight
        cropTopView = binding!!.cropTop
        cropBottomView = binding!!.cropBottom
        tool = binding!!.tool
        submitContainer = binding!!.submitContainer
        cancelButton = binding!!.cancelButton
        submitButton = binding!!.submitButton
        setContentView(binding!!.root)
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        initParam()
        resetCrop()
        resetPreviewSize()
        closeButton.setOnClickListener {
            this.finish()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        methodRequiresPermission()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
        cancelButton.setOnClickListener {
            cancelResultState()
        }
        submitButton.setOnClickListener {
            val result = saveBitmap()
            if (result.first) {
                val data = Intent()
                data.putExtra("filePath", result.second)
                data.putExtra("photoType", photoType)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    private fun initParam() {
        photoType = intent.getSerializableExtra("photoType") as PhotoTypeEnum
        userId = intent.getIntExtra("userId", 0)
    }

    private fun saveBitmap(): Pair<Boolean, String?> {
        var flag = false
        val photoName =  EnumUtils.getPhotoTypeEnum(photoType.value).name.lowercase(Locale.getDefault())
        //var path = "${getExternalFilesDir(null)?.path}/auth/images/${photoName}"
        var path = "${cacheDir}/auth/images/${userId}_${photoName}.jpeg"
        photo?.let {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            val folder = file.parentFile
            if (!folder.exists()) {
                folder.mkdirs()
            }
            file.createNewFile()
            var fOut: FileOutputStream? = null
            try {
                fOut = FileOutputStream(file)
                photo!!.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                flag = true
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            try {
                fOut!!.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                fOut!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return Pair<Boolean, String?>(flag, path)
    }

    private fun resetCrop() {
        val previewViewDrawable = when (photoType) {
            PhotoTypeEnum.ID_CARD_FRONT -> {
                R.drawable.camera_id_card_front
            }
            PhotoTypeEnum.ID_CARD_BACK -> {
                R.drawable.camera_id_card_back
            }else->{
                R.drawable.camera_id_card_front
            }
        }
        cropView.setImageResource(previewViewDrawable)
    }

    private fun resetPreviewSize() {
        val windowManager = windowManager
        val point = Point()
        windowManager.defaultDisplay.getRealSize(point)
        val width = point.x
        val height = point.y
        if (width.toDouble() / height.toDouble() >= 0.5625) {
            val cropContainerParams =
                LinearLayout.LayoutParams(
                    DimenUtils.dp2px(this, 270f).toInt(),
                    DimenUtils.dp2px(this, 428f).toInt()
                )
            cropView.layoutParams = cropContainerParams
            val captureContainerParams =
                LinearLayout.LayoutParams(
                    DimenUtils.dp2px(this, 60f).toInt(),
                    DimenUtils.dp2px(this, 60f).toInt()
                )
            captureButton.layoutParams = captureContainerParams
            val toolContainerParams =
                ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, DimenUtils.dp2px(this, 125f).toInt()
                ).apply {
                    leftToLeft = root.id
                    rightToRight = root.id
                    bottomToBottom = root.id
                }
            tool.layoutParams = toolContainerParams
            val submitContainerParams = LinearLayout.LayoutParams(
                DimenUtils.dp2px(this, 40f).toInt(),
                DimenUtils.dp2px(this, 40f).toInt()
            )
            submitButton.layoutParams = submitContainerParams
            val cancelContainerParams = LinearLayout.LayoutParams(
                DimenUtils.dp2px(this, 40f).toInt(),
                DimenUtils.dp2px(this, 40f).toInt()
            )
            cancelContainerParams.setMargins(0, 0, DimenUtils.dp2px(this, 80f).toInt(), 0)
            cancelButton.layoutParams = cancelContainerParams
        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        var preview: Preview = Preview.Builder()
            .build()
        var cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val imageAnalysis = ImageAnalysis.Builder()
            .build()
        imageAnalysis.setAnalyzer(
            cameraExecutor,
            ImageAnalysis.Analyzer { imageProxy ->
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                imageProxy.close()
            })
        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_90)
            .build()
        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation: Int = when (orientation) {
                    -1 -> Surface.ROTATION_90
                    in 0..44 -> Surface.ROTATION_0
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture.targetRotation = rotation
            }
        }
        orientationEventListener.enable()
        var camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner, cameraSelector, preview, imageAnalysis, imageCapture
        )
        val cameraControl = camera.cameraControl
        previewView.setOnTouchListener { _, motionEvent ->
            val meteringPoint = previewView.meteringPointFactory
                .createPoint(motionEvent.x, motionEvent.y)
            val action = FocusMeteringAction.Builder(meteringPoint)
                .build()
            cameraControl.startFocusAndMetering(action)
            true
        }
        captureButton.setOnClickListener {
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), object :
                ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val imageInfo = image.imageInfo
                    image.image?.let { originImage ->
                        val buffer = originImage!!.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        val bitmapTemp =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                        //裁切、旋转
                        val cropViewWidth = cropView.width
                        val cropViewHeight = cropView.height
                        //val cropViewBorder = cropView.strokeWidth
                        val left: Double = cropTopView.height.toDouble()
                        val top: Double = cropRightView.width.toDouble()
                        val leftProportion: Double = left / previewView.height
                        val topProportion: Double = top / previewView.width
                        val previewViewWidth = previewView.width.toDouble()
                        val previewViewHeight = previewView.height.toDouble()
                        val imageWidth = bitmapTemp.width
                        val imageHeight = bitmapTemp.height
                        var previewProportion = previewViewHeight / previewViewWidth
                        var imageProportion = imageWidth / imageHeight
                        var offsetProportion = imageProportion / previewProportion
                        var cropX: Double
                        var cropY: Double
                        var cropWidth: Double
                        var cropHeight: Double
                        cropX = leftProportion * imageWidth
                        cropY = topProportion * imageHeight
                        cropWidth =
                            cropViewHeight / previewViewHeight * imageWidth
                        cropHeight =
                            cropViewWidth / previewViewWidth * imageHeight
                        if (offsetProportion > 1) {
                            val shouldImageWidth = imageHeight * previewProportion
                            cropX =
                                (imageWidth - shouldImageWidth) / 2 + shouldImageWidth * leftProportion
                            cropWidth =
                                cropViewHeight / previewViewHeight * shouldImageWidth

                        } else {
                            val shouldImageHeight = imageWidth * (1 / previewProportion)
                            cropY =
                                (imageHeight - shouldImageHeight) / 2 + shouldImageHeight * topProportion
                            cropHeight =
                                cropViewWidth / previewViewWidth * shouldImageHeight
                        }
                        val previewMatrix = Matrix()
                        previewMatrix.preRotate(90f)
                        val bitmap = Bitmap.createBitmap(
                            bitmapTemp,
                            kotlin.math.floor(cropX).toInt(),
                            kotlin.math.floor(cropY).toInt(),
                            kotlin.math.ceil(cropWidth).toInt(),
                            kotlin.math.ceil(cropHeight).toInt(),
                            previewMatrix,
                            false
                        )
                        val originMatrix = Matrix()
                        originMatrix.preRotate(imageInfo.rotationDegrees.toFloat())
                        photo = Bitmap.createBitmap(
                            bitmapTemp,
                            kotlin.math.floor(cropX).toInt(),
                            kotlin.math.floor(cropY).toInt(),
                            kotlin.math.ceil(cropWidth).toInt(),
                            kotlin.math.ceil(cropHeight).toInt(),
                            originMatrix,
                            false
                        )
                        image.close()
                        previewView.visibility = View.GONE
                        Glide.with(this@CameraIdCardActivity).load(bitmap)
                            .addListener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable?>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable?>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    setResultState()
                                    return false
                                }
                            }).into(cropView)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                }
            })
        }
    }

    private fun setResultState() {
        closeButton.visibility = View.GONE
        captureButton.visibility = View.GONE
        submitContainer.visibility = View.VISIBLE
        orientationEventListener?.disable()
    }

    private fun cancelResultState() {
        closeButton.visibility = View.VISIBLE
        captureButton.visibility = View.VISIBLE
        submitContainer.visibility = View.GONE
        previewView.visibility = View.VISIBLE
        orientationEventListener?.enable()
        resetCrop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        cameraExecutor.shutdown()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        TODO("Not yet implemented")
    }

    @AfterPermissionGranted(3)
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
}