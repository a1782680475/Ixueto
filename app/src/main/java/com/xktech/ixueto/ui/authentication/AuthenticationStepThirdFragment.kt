package com.xktech.ixueto.ui.authentication

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.SelectorAdapter
import com.xktech.ixueto.components.PictureDialogFragment
import com.xktech.ixueto.databinding.FragmentAuthenticationStepThirdBinding
import com.xktech.ixueto.model.*
import com.xktech.ixueto.ui.camera.CameraIdCardActivity
import com.xktech.ixueto.utils.DimenUtils
import com.xktech.ixueto.utils.EnumUtils
import com.xktech.ixueto.utils.FileUtils
import com.xktech.ixueto.utils.RenderStateManager
import com.xktech.ixueto.viewModel.AuthenticationViewModel
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


@AndroidEntryPoint
class AuthenticationStepThirdFragment : Fragment() {
    private var binding: FragmentAuthenticationStepThirdBinding? = null
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var navController: NavController
    private lateinit var loading: ViewGroup
    private lateinit var uploadLoading: ViewGroup
    private lateinit var form: View
    private lateinit var submitButton: Button
    private lateinit var authenticationState: AuthenticationStateEnum
    private lateinit var authenticationConfigMap: Map<String, AuthenticationItemConfig>
    private lateinit var authenticationInfoMap: MutableMap<String, Any?>
    private var authenticationInfo: AuthenticationInfo? = null
    private lateinit var formComposeIdCard: ViewGroup
    private lateinit var idCard: TextInputLayout
    private lateinit var idCardFrontPhoto: ViewGroup
    private lateinit var idCardBackPhoto: ViewGroup
    private lateinit var formComposeRecent: ViewGroup
    private lateinit var recentPhoto: ViewGroup
    private lateinit var formComposeResidence: ViewGroup
    private lateinit var residenceIndexPhoto: ViewGroup
    private lateinit var residenceDetailPhoto: ViewGroup
    private lateinit var formComposeStandard: ViewGroup
    private lateinit var standardPhoto: ViewGroup
    private lateinit var formComposeEnterprise: LinearLayout
    private lateinit var enterpriseProvince: TextInputLayout
    private lateinit var enterpriseCity: TextInputLayout
    private lateinit var enterpriseCounty: TextInputLayout
    private lateinit var enterpriseStreet: TextInputLayout
    private lateinit var enterprise: TextInputLayout
    private lateinit var school: TextInputLayout
    private lateinit var department: TextInputLayout
    private lateinit var educational: TextInputLayout
    private lateinit var major: TextInputLayout
    private lateinit var classroom: TextInputLayout
    private lateinit var enrollmentYear: TextInputLayout
    private lateinit var graduationYear: TextInputLayout
    private lateinit var studentNumber: TextInputLayout
    private lateinit var formComposeStudentStatus: ViewGroup
    private lateinit var studentStatusPhoto: ViewGroup
    private lateinit var formComposeDiploma: ViewGroup
    private lateinit var diplomaPhoto: ViewGroup
    private lateinit var idCardFrontExample: TextView
    private lateinit var idCardBackExample: TextView
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val gson: Gson = Gson().newBuilder()
        .setLenient()
        .serializeNulls()
        .create()
    private var configKeyViewMap = mutableMapOf<String, Pair<View?, ViewGroup?>>()
    private var viewAuthenticationInfo = mutableMapOf<View, String>()
    private var photoViewTypeMap = mutableMapOf<View, PhotoTypeEnum>()
    private var userId: Int = 0
    private val renderStateManager = RenderStateManager()
    private var currentPhotoType: PhotoTypeEnum? = null
    private var currentPhotoViewGroup: ViewGroup? = null
    private var currentImageUrl: Uri? = null
    private var currentImagePath: String? = null
    private lateinit var cameraIdCardActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraNormalActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var pictureActivityLauncher: ActivityResultLauncher<Intent>
    private val errorImageResourceId: Int = R.drawable.image_error
    private var isFirstLoad = true
    override fun onCreate(savedInstanceState: Bundle?) {
        cameraIdCardActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    data?.apply {
                        val photoType = data.getSerializableExtra("photoType") as PhotoTypeEnum
                        val filePath = data.getStringExtra("filePath")
                        currentPhotoType?.let {
                            var imageView = currentPhotoViewGroup!!.children.first() as ImageView
                            uploadPhoto(photoType, filePath!!) { url ->
                                Glide.with(requireContext()).load(url)
                                    .skipMemoryCache(false)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .into(imageView)
                                currentPhotoViewGroup?.tag = url
                            }
                        }
                    }
                }
            }
        cameraNormalActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    currentPhotoType?.let {
                        val exifInterface = ExifInterface(currentImagePath!!)
                        val degree = readPictureDegree(exifInterface)
                        var bitmap = BitmapFactory.decodeFile(currentImagePath!!)
                        bitmap = onPictureTaken(bitmap, degree.toFloat())
                        var imageView = currentPhotoViewGroup!!.children.first() as ImageView
                        uploadPhoto(currentPhotoType!!, bitmap!!) { url ->
                            Glide.with(requireContext()).load(url)
                                .skipMemoryCache(false)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(imageView)
                            currentPhotoViewGroup?.tag = url
                        }
                    }
                }
            }
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
                        var imageView = currentPhotoViewGroup!!.children.first() as ImageView
                        uploadPhoto(currentPhotoType!!, bitmap!!) { url ->
                            Glide.with(requireContext()).load(url)
                                .skipMemoryCache(false)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(imageView)
                            currentPhotoViewGroup?.tag = url
                        }
                    }
                }
            }
        super.onCreate(savedInstanceState)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isFirstLoad) {
            isFirstLoad = false
            loadData()
        }
    }

    private fun initView() {
        binding = FragmentAuthenticationStepThirdBinding.inflate(layoutInflater)
        rootView = binding!!.root
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar = binding!!.toolBar!!
        toolBar.setupWithNavController(navController, appBarConfiguration)
        loading = binding!!.loading
        uploadLoading = binding!!.uploadLoading
        form = binding!!.form
        idCard = binding!!.idCard
        formComposeIdCard = binding!!.formComposeIdCard
        idCardFrontPhoto = binding!!.cardFrontPhoto
        idCardBackPhoto = binding!!.cardBackPhoto
        formComposeRecent = binding!!.formComposeRecent
        recentPhoto = binding!!.recentPhoto
        formComposeResidence = binding!!.formComposeResidence
        residenceIndexPhoto = binding!!.residenceIndexPhoto
        residenceDetailPhoto = binding!!.residenceDetailPhoto
        formComposeStandard = binding!!.formComposeStandard
        standardPhoto = binding!!.standardPhoto
        formComposeEnterprise = binding!!.formComposeEnterprise
        enterpriseProvince = binding!!.enterpriseProvince
        enterpriseCity = binding!!.enterpriseCity
        enterpriseCounty = binding!!.enterpriseCounty
        enterpriseStreet = binding!!.enterpriseStreet
        enterprise = binding!!.enterprise
        school = binding!!.school
        department = binding!!.department
        educational = binding!!.educational
        major = binding!!.major
        classroom = binding!!.classroom
        enrollmentYear = binding!!.enrollmentYear
        graduationYear = binding!!.graduationYear
        studentNumber = binding!!.studentNumber
        formComposeStudentStatus = binding!!.formComposeStudentStatus
        studentStatusPhoto = binding!!.studentStatusPhoto
        formComposeDiploma = binding!!.formComposeDiploma
        diplomaPhoto = binding!!.diplomaPhoto
        submitButton = binding!!.submitButton
        idCardFrontExample = binding!!.idCardFrontExample
        idCardBackExample = binding!!.idCardBackExample
        authenticationState =
            arguments?.getSerializable("authenticationState") as AuthenticationStateEnum
        authenticationConfigMap =
            arguments?.getSerializable("authenticationConfig") as Map<String, AuthenticationItemConfig>
        authenticationInfo = arguments?.getSerializable("authenticationInfo") as AuthenticationInfo
        idCardFrontExample.setOnClickListener {
            showImagePreviewDialog(R.drawable.example_id_card_front)
        }
        idCardBackExample.setOnClickListener {
            showImagePreviewDialog(R.drawable.example_id_card_back)
        }
    }

    private fun loadData() {
        userViewModel.getUserInfo().observe(viewLifecycleOwner) { userInfo ->
            userId = userInfo!!.userId
            renderStateManager.setOnAllRenderedListener {
                hiddenLoading()
            }
            initMapper()
            renderAuthority {
                setAuthenticationInfo {
                    initEnterpriseSelector()
                    initEducationalSelector()
                }
            }
            submitButton.setOnClickListener {
                if (submitVerify()) {
                    this.authenticationInfo = getFormValues()
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("提交确认")
                        .setMessage("认证信息提交后将进入审核，确认吗？")
                        .setNegativeButton(resources.getString(R.string.cancel), null)
                        .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                            submitData()
                        }
                        .show()
                }
            }
        }
    }

    private fun submitData() {
        showUploadLoading()
        authenticationViewModel.saveAuthenticationInfo(this.authenticationInfo!!)
            .observe(viewLifecycleOwner) {
                hideUploadLoading()
                if (it.data == true) {
                    navController.previousBackStackEntry!!.savedStateHandle["isSubmitted"] = true
                    navController.popBackStack()
                } else {
                    Snackbar.make(
                        rootView!!,
                        "提交失败，${it.msg}！",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun uploadPhoto(
        photoType: PhotoTypeEnum,
        filePath: String,
        callback: (String) -> Unit
    ) {
        showUploadLoading()
        authenticationViewModel.photoUpload(photoType, File(filePath)).observe(viewLifecycleOwner) {
            hideUploadLoading()
            callback(it)
        }
    }

    private fun uploadPhoto(
        photoType: PhotoTypeEnum,
        bitmap: Bitmap,
        callback: (String) -> Unit
    ) {
        showUploadLoading()
        authenticationViewModel.photoUpload(photoType, bitmap).observe(viewLifecycleOwner) {
            hideUploadLoading()
            callback(it)
        }
    }

    private fun showLoading() {
        loading.visibility = View.VISIBLE
    }

    private fun showUploadLoading() {
        uploadLoading.visibility = View.VISIBLE
    }

    private fun hideUploadLoading() {
        uploadLoading.visibility = View.GONE
    }

    private fun hiddenLoading() {
        form.visibility = View.VISIBLE
        loading.visibility = View.GONE
    }

    private fun initMapper() {
        configKeyViewMap = mutableMapOf(
            Pair("idCard", Pair(idCard, null)),
            Pair("idCardFront", Pair(idCardFrontPhoto, formComposeIdCard)),
            Pair("idCardBack", Pair(idCardBackPhoto, formComposeIdCard)),
            Pair("recent", Pair(recentPhoto, formComposeRecent)),
            Pair("hk1", Pair(residenceIndexPhoto, formComposeResidence)),
            Pair("hk2", Pair(residenceDetailPhoto, formComposeResidence)),
            Pair("standard", Pair(standardPhoto, formComposeStandard)),
            Pair("en-province", Pair(enterpriseProvince, formComposeEnterprise)),
            Pair("en-city", Pair(enterpriseCity, formComposeEnterprise)),
            Pair("en-county", Pair(enterpriseCounty, formComposeEnterprise)),
            Pair("en-street", Pair(enterpriseStreet, formComposeEnterprise)),
            Pair("enterpriseName", Pair(enterprise, formComposeEnterprise)),
            Pair("education", Pair(educational, null)),
            Pair("schoolName", Pair(school, null)),
            Pair("departmentName", Pair(department, null)),
            Pair("majorName", Pair(major, null)),
            Pair("className", Pair(classroom, null)),
            Pair("enrollmentYear", Pair(enrollmentYear, null)),
            Pair("graduationYear", Pair(graduationYear, null)),
            Pair("stuNumber", Pair(studentNumber, null)),
            Pair("studentStatus", Pair(studentStatusPhoto, formComposeStudentStatus)),
            Pair("diploma", Pair(diplomaPhoto, formComposeDiploma)),
        )
        viewAuthenticationInfo =
            mutableMapOf(
                Pair(idCard, "IdCard"),
                Pair(idCardFrontPhoto, "IdCardFrontPhoto"),
                Pair(idCardBackPhoto, "IdCardBackPhoto"),
                Pair(recentPhoto, "RecentPhoto"),
                Pair(residenceIndexPhoto, "ResidenceIndexPhoto"),
                Pair(residenceDetailPhoto, "ResidenceDetailPhoto"),
                Pair(standardPhoto, "StandardPhoto"),
                Pair(enterpriseProvince, "EnProvinceId"),
                Pair(enterpriseCity, "EnCityId"),
                Pair(enterpriseCounty, "EnCountyId"),
                Pair(enterpriseStreet, "EnStreetId"),
                Pair(enterprise, "Enterprise"),
                Pair(educational, "Educational"),
                Pair(school, "School"),
                Pair(department, "Department"),
                Pair(major, "Major"),
                Pair(classroom, "Class"),
                Pair(enrollmentYear, "EnrollmentYear"),
                Pair(graduationYear, "GraduationYear"),
                Pair(studentNumber, "StudentNumber"),
                Pair(studentStatusPhoto, "StudentStatusPhoto"),
                Pair(diplomaPhoto, "DiplomaPhoto"),
            )
        photoViewTypeMap = mutableMapOf(
            Pair(idCardFrontPhoto, PhotoTypeEnum.ID_CARD_FRONT),
            Pair(idCardBackPhoto, PhotoTypeEnum.ID_CARD_BACK),
            Pair(recentPhoto, PhotoTypeEnum.RECENT),
            Pair(residenceIndexPhoto, PhotoTypeEnum.RESIDENCE_INDEX),
            Pair(residenceDetailPhoto, PhotoTypeEnum.RESIDENCE_DETAIL),
            Pair(standardPhoto, PhotoTypeEnum.STANDARD),
            Pair(studentStatusPhoto, PhotoTypeEnum.STUDENT_STATUS),
            Pair(diplomaPhoto, PhotoTypeEnum.DIPLOMA),
        )
        authenticationInfo?.let {
            authenticationInfoMap =
                gson.fromJson<MutableMap<String, Any?>>(
                    gson.toJson(authenticationInfo),
                    MutableMap::class.java
                )
        }
    }

    private fun setAuthenticationInfo(callback: () -> Unit) {
        authenticationInfo?.let {
            authenticationConfigMap.forEach { configMap ->
                val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
                val view = formItemAndComposePair.first
                view?.let {
                    val formItemConfig = configMap.value
                    val value = viewAuthenticationInfo[view]
                    when (formItemConfig.Type) {
                        0 -> {
                            view as TextInputLayout
                            authenticationInfoMap[value]?.let { formValue ->
                                if (formValue is Number) {
                                    view.editText?.setText(formValue.toInt().toString())
                                } else {
                                    view.editText?.setText(formValue.toString())
                                }
                            }
                        }
                        1 -> {

                        }
                        2 -> {
                            if (view.visibility == View.VISIBLE) {
                                //renderStateManager.register(configMap.key)
                                view as ViewGroup
                                val imageView = view.children.first() as ImageView
                                val coverView = view.children.elementAt(1) as TextView
                                val value = authenticationInfoMap[value]
                                if (!value.toString().isNullOrEmpty()) {
                                    view.tag = value
                                    Glide.with(requireContext()).asBitmap()
                                        .load(value)
                                        .placeholder(R.drawable.ic_placeholder)
                                        .transition(BitmapTransitionOptions().crossFade())
                                        .error(errorImageResourceId)
                                        .addListener(object : RequestListener<Bitmap?> {
                                            override fun onResourceReady(
                                                resource: Bitmap?,
                                                model: Any?,
                                                target: Target<Bitmap?>?,
                                                dataSource: DataSource?,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                if (coverView.visibility == View.VISIBLE) {
                                                    coverView.layoutParams =
                                                        ConstraintLayout.LayoutParams(
                                                            view?.width!!,
                                                            ((resource!!.height / resource!!.width.toDouble()) * view.width).toInt()
                                                        ).apply {
                                                            leftToLeft = view.id
                                                            rightToRight = view.id
                                                            topToTop = view.id
                                                            bottomToBottom = view.id
                                                        }
                                                }
                                                //renderStateManager.rendered(configMap.key)
                                                return false
                                            }

                                            override fun onLoadFailed(
                                                e: GlideException?,
                                                model: Any?,
                                                target: Target<Bitmap?>?,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                if (coverView.visibility == View.VISIBLE) {
                                                    val imagePlaceHolder =
                                                        ContextCompat.getDrawable(
                                                            context!!,
                                                            errorImageResourceId
                                                        )
                                                    coverView.layoutParams =
                                                        ConstraintLayout.LayoutParams(
                                                            view.width,
                                                            ((imagePlaceHolder!!.minimumHeight / imagePlaceHolder!!.minimumWidth.toDouble()) * view.width).toInt()
                                                        ).apply {
                                                            leftToLeft = view.id
                                                            rightToRight = view.id
                                                            topToTop = view.id
                                                            bottomToBottom = view.id
                                                        }
                                                }
                                                //renderStateManager.rendered(configMap.key)
                                                return false
                                            }
                                        }).into(imageView)
                                } else {
                                    if (coverView.visibility == View.VISIBLE) {
                                        val imagePlaceHolder = imageView.drawable
                                        coverView.layoutParams =
                                            ConstraintLayout.LayoutParams(
                                                view.width,
                                                ((imagePlaceHolder!!.minimumHeight / imagePlaceHolder!!.minimumWidth.toDouble()) * view.width).toInt()
                                            ).apply {
                                                leftToLeft = view.id
                                                rightToRight = view.id
                                                topToTop = view.id
                                                bottomToBottom = view.id
                                            }
                                    }
                                    //renderStateManager.rendered(configMap.key)
                                }
                                view.setOnClickListener {
                                    currentPhotoType = photoViewTypeMap[view]!!
                                    currentPhotoViewGroup = view
                                    showPictureDialog(currentPhotoType!!)
                                }
                            }
                        }
                    }
                }
            }
        }
        callback()
    }

    private fun initEnterpriseSelector() {
        var defaultValue = authenticationInfo?.EnProvinceId
        loadEnProvinceData(defaultValue) {
            renderStateManager.rendered(it)
        }
        loadEnCityData(authenticationInfo?.EnProvinceId, authenticationInfo?.EnCityId) {
            renderStateManager.rendered(it)
        }
        loadEnCountyData(authenticationInfo?.EnCityId, authenticationInfo?.EnCountyId) {
            renderStateManager.rendered(it)
        }
        loadEnStreetData(authenticationInfo?.EnCountyId, authenticationInfo?.EnStreetId) {
            renderStateManager.rendered(it)
        }
    }

    private fun showImagePreviewDialog(drawableId: Int) {
        val imageDialog =
            MaterialAlertDialogBuilder(requireContext()).setView(R.layout.dialog_image_preview)
                .create()
        imageDialog.window?.setLayout(
            DimenUtils.dp2px(requireContext(), 380f).toInt(),
            DimenUtils.dp2px(requireContext(), 450f).toInt()
        )
        imageDialog.show()
        val imageView = imageDialog.findViewById<ShapeableImageView>(R.id.image_view)
        imageView!!.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                drawableId
            )
        )
    }

    private fun initEducationalSelector() {
        var defaultValue = authenticationInfo?.Educational
        loadEducationalData(defaultValue) {
            renderStateManager.rendered(it)
        }
    }

    private fun loadEnProvinceData(defaultValue: String?, rendered: ((String) -> Unit)?) {
        val selectorFlag = "enProvince"
        renderStateManager.register(selectorFlag)
        authenticationViewModel.province.observe(viewLifecycleOwner) { provinces ->
            renderSelector(
                enterpriseProvince,
                provinces,
                defaultValue,
                rendered = {
                    if (rendered != null) {
                        rendered(selectorFlag)
                    }
                },
                selected = { isChanged, selectedValue ->
                    if (isChanged) {
                        clearSelector(
                            mutableListOf(
                                enterpriseCity,
                                enterpriseCounty,
                                enterpriseStreet
                            )
                        )
                    }
                    loadEnCityData(selectedValue, null, null)
                })
        }
    }

    private fun loadEnCityData(
        provinceId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "enCity"
        renderStateManager.register(selectorFlag)
        if (!provinceId.isNullOrEmpty()) {
            authenticationViewModel.city(provinceId).observe(viewLifecycleOwner) { cities ->
                renderSelector(
                    enterpriseCity,
                    cities,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->
                        if (isChanged) {
                            clearSelector(mutableListOf(enterpriseCounty, enterpriseStreet))
                        }
                        loadEnCountyData(selectedValue, null, null)
                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }

    private fun loadEnCountyData(
        cityId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "enCounty"
        renderStateManager.register(selectorFlag)
        if (!cityId.isNullOrEmpty()) {
            authenticationViewModel.county(cityId).observe(viewLifecycleOwner) { counties ->
                renderSelector(
                    enterpriseCounty,
                    counties,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->
                        if (isChanged) {
                            clearSelector(mutableListOf(enterpriseStreet))
                        }
                        loadEnStreetData(selectedValue, null, null)
                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }

    private fun loadEnStreetData(
        countyId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "enStreet"
        renderStateManager.register(selectorFlag)
        if (!countyId.isNullOrEmpty()) {
            authenticationViewModel.street(countyId).observe(viewLifecycleOwner) { streets ->
                renderSelector(
                    enterpriseStreet,
                    streets,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->

                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }

    private fun loadEducationalData(defaultValue: String?, rendered: ((String) -> Unit)?) {
        val selectorFlag = "educational"
        renderStateManager.register(selectorFlag)
        renderSelector(
            educational,
            mutableListOf(
                Selector("本科", "本科"),
                Selector("专科", "专科"),
                Selector("技师", "技师"),
                Selector("高职", "高职"),
                Selector("技工", "技工"),
                Selector("职专", "职专")
            ),
            defaultValue,
            rendered = {
                if (rendered != null) {
                    rendered(selectorFlag)
                }
            },
            selected = { isChanged, selectedValue ->

            })
    }

    private fun showPictureDialog(photoType: PhotoTypeEnum) {
        val modalBottomSheet = PictureDialogFragment.newInstance()
        modalBottomSheet.setOnCameraClickListener {
            when (photoType) {
                PhotoTypeEnum.ID_CARD_FRONT,
                PhotoTypeEnum.ID_CARD_BACK -> {
                    cameraIdCardActivityLauncher.launch(
                        Intent(
                            context,
                            CameraIdCardActivity::class.java
                        )
                            .putExtra("userId", userId)
                            .putExtra("photoType", photoType)
                    )
                }
                else -> {
                    startNativeCamera(photoType)
                }
            }
        }
        modalBottomSheet.setOnPictureClickListener {
            pictureActivityLauncher.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            )
        }
        modalBottomSheet.show(
            requireActivity().supportFragmentManager,
            PictureDialogFragment.TAG
        )
    }

    private fun startNativeCamera(photoType: PhotoTypeEnum) {
        val photoName =
            EnumUtils.getPhotoTypeEnum(photoType.value).name.lowercase(Locale.getDefault())
        var path = "${activity?.cacheDir}/auth/images/${userId}_${photoName}.jpeg"
        currentImagePath = path
        val outputImage = File(path)
        if (!outputImage.parentFile.exists()) {
            outputImage.parentFile.mkdirs()
        }
        if (outputImage.exists()) {
            outputImage.delete()
        }
        val imageUri = FileProvider.getUriForFile(
            requireActivity(),
            "com.xktech.ixueto.fileprovider", outputImage
        )
        currentImageUrl = imageUri
        outputImage.createNewFile()
        cameraNormalActivityLauncher.launch(
            Intent(
                MediaStore.ACTION_IMAGE_CAPTURE,
            ).putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        )
    }

    private fun renderAuthority(callback: () -> Unit) {
        authenticationConfigMap.forEach { configMap ->
            val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
            var formItem: View? = formItemAndComposePair.first
            var compose: View? = formItemAndComposePair.second
            formItem?.let {
                val formItemConfig = configMap.value
                //可否显示
                if (formItemConfig.Show) {
                    formItem.visibility = View.VISIBLE
                    compose?.let {
                        compose.visibility = View.VISIBLE
                    }
                } else {
                    formItem.visibility = View.GONE
                    compose?.let {
                        compose.visibility = View.GONE
                    }
                }
                //可否编辑
                formItem.isEnabled = formItemConfig.Editable
                when (formItemConfig.Type) {
                    2 -> {
                        formItem as ViewGroup
                        val coverView = formItem.children.elementAt(1) as TextView
                        if (formItemConfig.Editable) {
                            coverView.visibility = View.GONE
                        } else {
                            coverView.visibility = View.VISIBLE
                        }
                    }
                    else -> {

                    }
                }
                //是否必填
                if (formItemConfig.Verify == "disabled") {
                    when (formItemConfig.Type) {
                        0, 1 -> {
                            formItem as TextInputLayout
                            formItem.hint = formItem.hint.toString() + "(可选)"
                        }
                        2 -> {
                            (((formItem.parent as ViewGroup)?.getChildAt(1) as ViewGroup).getChildAt(
                                0
                            ) as TextView).apply {
                                text = "$text(可选)"
                            }
                        }
                    }
                }
            }
        }
        callback()
    }

    private fun getFormItemAndComposePair(itemName: String): Pair<View?, ViewGroup?> {
        var viewPair: Pair<View?, ViewGroup?>? = try {
            configKeyViewMap[itemName]
        } catch (e: ArithmeticException) {
            null
        }
        return viewPair ?: Pair(null, null)
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

    private fun getImagePath(uri: Uri): String? {
        var path: String? = null
        val cursor: Cursor? = requireContext().contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
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

    private fun renderSelector(
        view: TextInputLayout,
        list: MutableList<Selector>,
        defaultValue: String?,
        rendered: (() -> Unit)?,
        selected: ((Boolean, String) -> Unit)?
    ) {
        val selectorAdapter = SelectorAdapter(requireContext(), list)
        if (!defaultValue.isNullOrEmpty()) {
            selectorAdapter.value = defaultValue
            for (item in list) {
                if (item.Value == defaultValue) {
                    view.editText?.setText(item.Title)
                }
            }
        }
        val materialAutoCompleteTextView = (view.editText as? MaterialAutoCompleteTextView)
        materialAutoCompleteTextView?.setAdapter(selectorAdapter)
        materialAutoCompleteTextView?.onItemClickListener =
            OnItemClickListener { parent, _, position, _ ->
                val value = list[position].Value
                val isChange = (parent.adapter as SelectorAdapter).value != value
                (parent.adapter as SelectorAdapter).value = value
                selected?.let {
                    selected(isChange, value)
                }
            }
        rendered?.let {
            rendered()
        }
    }

    private fun clearSelector(views: List<TextInputLayout>) {
        views.forEach {
            (it.editText as? MaterialAutoCompleteTextView)?.setAdapter(null)
            it.editText?.text?.clear()
        }
    }

    private fun submitVerify(): Boolean {
        var verifyPass = true
        var errorColorId = R.color.md_theme_light_error
        var normalColorId = R.color.md_theme_light_onBackground
        if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            errorColorId = R.color.md_theme_dark_error
            normalColorId = R.color.md_theme_dark_onBackground
        }
        var errorColor = ContextCompat.getColor(requireContext(), errorColorId)
        var normalColor = ContextCompat.getColor(requireContext(), normalColorId)
        authenticationConfigMap.filter {
            it.value.Show
        }.forEach { configMap ->
            val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
            var formItem: View? = formItemAndComposePair.first
            formItem?.let {
                val formItemConfig = configMap.value
                if (formItemConfig.Verify != "disabled") {
                    val verifyString = formItemConfig.Verify
                    val verifyArray = verifyString.split("|")
                    when (formItemConfig.Type) {
                        0 -> {
                            formItem as TextInputLayout
                            val text = formItem.editText?.text
                            var error: String? = null
                            if ("required" in verifyArray) {
                                if (text?.trim().isNullOrEmpty()) {
                                    verifyPass = false
                                    error = "此项不能为空"
                                } else if ("number" in verifyArray) {
                                    if (!Pattern.matches("^[0-9]*\$", text?.trim())) {
                                        verifyPass = false
                                        error = "请输入数字"
                                    }
                                } else if ("phone" in verifyArray) {
                                    if (!Pattern.matches(
                                            "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}\$",
                                            text?.trim()
                                        )
                                    ) {
                                        verifyPass = false
                                        error = "请输入正确电话号码"
                                    }
                                }
                            }
                            if (error.isNullOrEmpty()) {
                                formItem.error = null
                            } else {
                                formItem.error = error
                            }
                            formItem.isErrorEnabled = !formItem.error.isNullOrEmpty()
                        }
                        1 -> {
                            formItem as TextInputLayout
                            val text = formItem.editText?.text
                            var error: String? = null
                            if ("required" in verifyArray || "dropDownList" in verifyArray) {
                                if (text?.trim().isNullOrEmpty()) {
                                    verifyPass = false
                                    error = "此项不能为空"
                                }
                            }
                            if (error.isNullOrEmpty()) {
                                formItem.error = null
                            } else {
                                formItem.error = error
                            }
                            formItem.isErrorEnabled = !formItem.error.isNullOrEmpty()
                        }
                        2 -> {
                            formItem as ViewGroup
                            if (formItem.tag == null || formItem.tag.toString().isNullOrEmpty()) {
                                verifyPass = false
                                (((formItem.parent as ViewGroup)?.getChildAt(1) as ViewGroup).getChildAt(
                                    0
                                ) as TextView).apply {
                                    setTextColor(errorColor)
                                }
                                val imageView = formItem.children.first() as ShapeableImageView
                                imageView.strokeWidth = 6f
                                imageView.strokeColor =
                                    resources.getColorStateList(errorColorId, null)
                                formItem.isFocusable = true
                                formItem.isFocusableInTouchMode = true
                                formItem.requestFocus()
                                formItem.requestFocusFromTouch()
                            } else {
                                (((formItem.parent as ViewGroup)?.getChildAt(1) as ViewGroup).getChildAt(
                                    0
                                ) as TextView).apply {
                                    setTextColor(normalColor)
                                }
                                val imageView = formItem.children.first() as ShapeableImageView
                                imageView.strokeWidth = 0f
                            }
                        }
                    }
                } else {
                    when (formItemConfig.Type) {
                        0, 1 -> {
                            formItem as TextInputLayout
                            formItem.isErrorEnabled = false
                        }
                        2 -> {

                        }
                    }
                }
            }
        }
        return verifyPass
    }

    private fun getFormValues(): AuthenticationInfo {
        authenticationConfigMap.forEach { configMap ->
            val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
            val view = formItemAndComposePair.first
            view?.let {
                val formItemConfig = configMap.value
                val formName = viewAuthenticationInfo[view]
                if (!formName.isNullOrEmpty()) {
                    if (formItemConfig.Show) {
                        val formValue: String? = when (formItemConfig.Type) {
                            0 -> {
                                view as TextInputLayout
                                if (!view.editText?.text.toString().trim()
                                        .isNullOrEmpty()
                                ) view.editText?.text.toString()
                                else null
                            }
                            1 -> {
                                view as TextInputLayout
                                (view.editText as? MaterialAutoCompleteTextView)?.adapter?.let {
                                    val selectorAdapter =
                                        it as SelectorAdapter
                                    selectorAdapter.value
                                }
                            }
                            2 -> {
                                view as ViewGroup
                                view.tag?.let {
                                    view.tag.toString()
                                }
                            }
                            else -> {
                                null
                            }
                        }
                        authenticationInfoMap[formName!!] = formValue
                    }
                }
            }
        }
        var formValuesJson = gson.toJson(authenticationInfoMap)
        return gson.fromJson(formValuesJson, AuthenticationInfo::class.java)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            AuthenticationStepTwoFragment()
    }
}