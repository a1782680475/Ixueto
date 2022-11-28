package com.xktech.ixueto.ui.feedback

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.shape.CornerFamily
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.xktech.ixueto.R
import com.xktech.ixueto.data.local.converter.DateConverter
import com.xktech.ixueto.databinding.FragmentFeedbackAddBinding
import com.xktech.ixueto.model.DeviceInfo
import com.xktech.ixueto.utils.DeviceUtils
import com.xktech.ixueto.utils.DimenUtils
import com.xktech.ixueto.utils.FileUtils
import com.xktech.ixueto.viewModel.FeedbackViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class FeedbackAddFragment : Fragment() {
    private lateinit var navController: NavController
    private var binding: FragmentFeedbackAddBinding? = null
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var imageAdd: Button
    private lateinit var imageContainer: FlexboxLayout
    private lateinit var sendDeviceSwitch: MaterialSwitch
    private lateinit var submitButton: Button
    private lateinit var titleText: TextInputLayout
    private lateinit var contentText: TextInputLayout
    private var errorColorId: Int = 0
    private lateinit var pictureActivityLauncher: ActivityResultLauncher<Intent>
    private val feedbackViewModel: FeedbackViewModel by viewModels()
    private var imageViewWidth = 0
    private var imageMargin = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pictureActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    data?.let {
                        val path = FileUtils.getFileAbsolutePath(requireContext(), data.data!!)
                        val exifInterface = ExifInterface(path!!)
                        val degree = readPictureDegree(exifInterface)
                        var bitmap = BitmapFactory.decodeFile(path)
                        bitmap = onPictureTaken(bitmap, degree.toFloat())
                        uploadPhoto(bitmap!!) { url ->
                            addUrlToImageContainer(url)
                        }
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            initView()
        }
        return rootView
    }

    private fun initView() {
        binding = FragmentFeedbackAddBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        imageAdd = binding!!.imageAdd
        imageContainer = binding!!.imageContainer
        sendDeviceSwitch = binding!!.sendDeviceSwitch
        submitButton = binding!!.submitButton
        titleText = binding!!.title
        contentText = binding!!.content
        errorColorId = R.color.md_theme_light_error
        if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            errorColorId = R.color.md_theme_dark_error
        }
        submitButton.setOnClickListener {
            if (submitVerify()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提交确认")
                    .setMessage("是否确定要提交反馈？")
                    .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->

                    }
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        val title = titleText.editText!!.text.trim().toString()
                        val content = contentText.editText!!.text.trim().toString()
                        val imageViewList = getUploadImageUrlList()
                        val deviceInfo = getDeviceInfo()
                        feedbackViewModel.addFeedback(title, content, imageViewList, deviceInfo)
                            .observe(viewLifecycleOwner) {
                                Snackbar.make(
                                    rootView!!,
                                    "已提交，感谢您的反馈！",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .show()
            }
        }
    }

    private fun addUrlToImageContainer(url: String) {
        val currentImageView = createPreviewImageView(imageContainer.childCount)
        imageContainer.addView(currentImageView)
        currentImageView.setOnLongClickListener { view ->
            view as ShapeableImageView
            setDeleteSelectedState(view)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除确认")
                .setMessage("是否要删除此图片？")
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                    cancelDeleteSelectedState(view)
                }
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    cancelDeleteSelectedState(view)
                    view.animate().alpha(0f).setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                var removeIndex = imageContainer.indexOfChild(currentImageView)
                                imageContainer.removeView(view)
                                resetImageView(
                                    currentImageView,
                                    removeIndex
                                )
                            }
                        })
                }
                .setOnCancelListener {
                    cancelDeleteSelectedState(view)
                }
                .show()
            false
        }
        Glide.with(requireContext()).load(url)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(currentImageView)
        currentImageView.tag = url
    }

    private fun setDeleteSelectedState(imageView: ShapeableImageView) {
        imageView.strokeColor = resources.getColorStateList(errorColorId, null)
        imageView.strokeWidth = 6f
        imageView.setPadding(3, 3, 3, 3)
    }

    private fun cancelDeleteSelectedState(imageView: ShapeableImageView) {
        imageView.strokeWidth = 0f
        imageView.setPadding(0, 0, 0, 0)
    }

    private fun resetImageView(view: View, removeIndex: Int) {
        for (index in removeIndex until imageContainer.childCount) {
            if (index % 2 == 0) {
                (imageContainer.children.elementAt(index).layoutParams as FlexboxLayout.LayoutParams).apply {
                    leftMargin = 0
                    rightMargin = imageMargin
                }
            } else {
                (imageContainer.children.elementAt(index).layoutParams as FlexboxLayout.LayoutParams).apply {
                    rightMargin = 0
                    leftMargin = imageMargin
                }
            }
        }
    }

    private fun createPreviewImageView(currentImageViewIndex: Int): ShapeableImageView {
        val imageView = ShapeableImageView(requireContext())
        if (currentImageViewIndex % 2 == 0) {
            imageView.layoutParams =
                FlexboxLayout.LayoutParams(imageViewWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply {
                        rightMargin = imageMargin
                        bottomMargin = 2 * imageMargin
                        flexShrink = 0f
                    }
        } else {
            imageView.layoutParams =
                FlexboxLayout.LayoutParams(imageViewWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply {
                        leftMargin = imageMargin
                        bottomMargin = 2 * imageMargin
                        flexShrink = 0f
                    }
        }
        val cornerSize = DimenUtils.dp2px(requireContext(), 10F)
        imageView.shapeAppearanceModel = imageView.shapeAppearanceModel.toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
            .setBottomLeftCorner(CornerFamily.ROUNDED, cornerSize)
            .setBottomRightCorner(CornerFamily.ROUNDED, cornerSize)
            .build()
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        return imageView
    }

    private fun getUploadImageUrlList(): MutableList<String> {
        val imageList = mutableListOf<String>()
        for (imageView in imageContainer.children) {
            imageList.add(imageView.tag.toString())
        }
        return imageList
    }

    private fun submitVerify(): Boolean {
        var verifyPass = true
        if (titleText.editText!!.text.trim().isNullOrEmpty()) {
            verifyPass = false
            titleText.error = "此项不能为空"
            titleText.isErrorEnabled = true
        } else {
            titleText.isErrorEnabled = false
            if (contentText.editText!!.text.trim().isNullOrEmpty()) {
                verifyPass = false
                contentText.error = "此项不能为空"
                contentText.isErrorEnabled = true
            } else {
                contentText.isErrorEnabled = false
            }
        }
        return verifyPass
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        imageAdd.setOnClickListener {
            pictureActivityLauncher.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            )
        }
        imageMargin = DimenUtils.dp2px(requireContext(), 5F).toInt()
        imageViewWidth =
            ((getWidthPixels() - requireContext().resources.displayMetrics.density * 40) / 2).toInt() - imageMargin
    }

    private fun readPictureDegree(exifInterface: ExifInterface): Int {
        var degree = -1
        try {
            val orientation: Int = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                ExifInterface.ORIENTATION_NORMAL -> 0
                ExifInterface.ORIENTATION_UNDEFINED -> -1
                else -> {
                    -1
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    private fun getWidthPixels(): Int {
        val dm: DisplayMetrics = resources.displayMetrics
        return dm.widthPixels
    }

    private fun onPictureTaken(bitmap: Bitmap, rotate: Float): Bitmap {
        if (rotate != -1f) {
            val matrix = Matrix()
            matrix.preRotate(rotate)
            return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap
                    .height, matrix, true
            )
        }
        return bitmap
    }

    private fun uploadPhoto(
        bitmap: Bitmap,
        callback: (String) -> Unit
    ) {
        feedbackViewModel.imageUpload(bitmap).observe(viewLifecycleOwner) {
            callback(it)
        }
    }

    private fun getDeviceInfo(): DeviceInfo? {
        var deviceInfo: DeviceInfo? = null
        if (sendDeviceSwitch.isChecked) {
            deviceInfo = DeviceInfo()
            DeviceUtils(requireActivity())
            val dm: DisplayMetrics = resources.displayMetrics
            deviceInfo.deviceManufacturer = DeviceUtils.getManufacturer()
            deviceInfo.deviceModel = DeviceUtils.getModel()
            deviceInfo.versionCode = DeviceUtils.getVersionCode()
            deviceInfo.versionName = DeviceUtils.getVerName()
            deviceInfo.androidVersionName = DeviceUtils.getSDKVersionName()
            deviceInfo.androidID = DeviceUtils.getAndroidID()
            deviceInfo.time = DateConverter.converterDate(Date())
            deviceInfo.displayMetrics = "${dm.widthPixels}*${dm.heightPixels}"
            deviceInfo.scaledDensity = dm.scaledDensity
        }
        return deviceInfo
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            FeedbackAddFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}