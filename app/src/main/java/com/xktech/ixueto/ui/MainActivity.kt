package com.xktech.ixueto.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.gyf.immersionbar.ktx.immersionBar
import com.vmadalin.easypermissions.EasyPermissions
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.ActivityMainBinding
import com.xktech.ixueto.model.Version
import com.xktech.ixueto.utils.DarkModelUtils
import com.xktech.ixueto.utils.NetWorkUtils
import com.xktech.ixueto.viewModel.MainActivityViewModel
import com.xktech.ixueto.viewModel.SettingViewModel
import com.xktech.ixueto.viewModel.VersionViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import pub.devrel.easypermissions.AfterPermissionGranted
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()
    private val settingViewModel: SettingViewModel by viewModels()
    private lateinit var btnUpdate: Button
    private lateinit var textProgress: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var textVersion: TextView
    private lateinit var textLog: TextView
    private lateinit var versionDialog: AlertDialog
    private lateinit var installFile: File
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var scaledDensity = 0f

    @Inject
    lateinit var okHttpClient: OkHttpClient
    private val versionViewModel: VersionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.root.viewTreeObserver.addOnPreDrawListener { //binding.root.viewTreeObserver.removeOnPreDrawListener(this)
//            false
//        }
        setDensity()
        bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
        createNotificationChannel()
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            }
        if (mainActivityViewModel.isFirstLoad) {
            mainActivityViewModel.isFirstLoad = false
            if (!NetWorkUtils.isInternetAvailable(this)) {
                Snackbar.make(binding.root, "请检查网络连接！", Snackbar.LENGTH_LONG)
                    .show()
            }
            methodRequiresPermission()
            mainActivityViewModel.trySetToken()
            var settingData = settingViewModel.getSettingSync()
            var startupPageIndex = if (!settingData.hasStartupPage()) {
                0
            } else {
                settingData.startupPage
            }
            when (startupPageIndex) {
                0 -> {
                    bottomNavigationView.selectedItemId = R.id.indexFragment
                }
                1 -> {
                    bottomNavigationView.selectedItemId = R.id.studyFragment
                }
            }
            versionCheck(this)
        }
        resetUI()
    }

    private fun hiddenBottomNavigationView() {
        bottomNavigationView.visibility = View.GONE
    }

    private fun showBottomNavigationView() {
        bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
    }

    private fun resetUI() {
        val isDark = DarkModelUtils.check(this)
//        immersionBar {
//            transparentStatusBar()
//        }
        if (isDark) {
            immersionBar {
                transparentStatusBar()
                statusBarDarkFont(false)
                navigationBarColor(R.color.md_theme_dark_background)
                navigationBarDarkIcon(false)
            }
        } else {
            immersionBar {
                transparentStatusBar()
                statusBarDarkFont(true)
                navigationBarColor(R.color.md_theme_light_surface_navigation)
                navigationBarDarkIcon(true)
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.indexFragment, R.id.studyFragment, R.id.mineFragment -> {
                    showBottomNavigationView()
                    if (isDark) {
                        immersionBar {
                            statusBarColor(R.color.md_theme_dark_background)
                            statusBarDarkFont(false)
                            navigationBarColor(R.color.md_theme_dark_surface_navigation)
                            navigationBarDarkIcon(false)
                        }
                    } else {
                        immersionBar {
                            statusBarColor(R.color.md_theme_light_background)
                            statusBarDarkFont(true)
                            navigationBarColor(R.color.md_theme_light_surface_navigation)
                            navigationBarDarkIcon(true)
                        }
                    }
                }
                R.id.courseStudyFragment -> {
                    hiddenBottomNavigationView()
                    immersionBar {
                        statusBarColor(R.color.black)
                        statusBarDarkFont(false)
                    }
                    if (isDark) {
                        immersionBar {
                            navigationBarColor(R.color.md_theme_dark_background)
                            navigationBarDarkIcon(false)
                        }
                    } else {
                        immersionBar {
                            navigationBarColor(R.color.md_theme_light_background)
                            navigationBarDarkIcon(true)
                        }
                    }
                }
                else -> {
                    hiddenBottomNavigationView()
                    if (isDark) {
                        immersionBar {
                            transparentStatusBar()
                            statusBarDarkFont(false)
                            navigationBarColor(R.color.md_theme_dark_background)
                            navigationBarDarkIcon(false)
                        }
                    } else {
                        immersionBar {
                            transparentStatusBar()
                            statusBarDarkFont(true)
                            navigationBarColor(R.color.md_theme_light_background)
                            navigationBarDarkIcon(true)
                        }
                    }
                }
            }
        }
        if (!mainActivityViewModel.isVersionChecked) {
            versionCheck(this)
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("resetStudy", "resetStudy", importance).apply {
                description = "学习重置通知"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun versionCheck(context: Context) {
        val packageManager = packageManager
        val packageInfo = packageManager!!.getPackageInfo(packageName, 0)
        val versionCode = packageInfo.versionCode
        versionViewModel.version.observe(this) { version ->
            if (!mainActivityViewModel.isVersionChecked) {
                mainActivityViewModel.isVersionChecked = true
                version?.let {
                    if (version.VersionCode > versionCode) {
                        showDialogUpdate(context, version)
                    }
                }
            }
        }
    }

    private fun showDialogUpdate(context: Context, version: Version) {
        versionDialog =
            MaterialAlertDialogBuilder(this).setView(R.layout.dialog_version).create()
        versionDialog.setCanceledOnTouchOutside(false)
        versionDialog.setCancelable(false)
        versionDialog.show()
        btnUpdate = versionDialog.findViewById(R.id.version_update)!!
        textProgress = versionDialog.findViewById(R.id.progress)!!
        progressBar = versionDialog.findViewById(R.id.progress_bar)!!
        textVersion = versionDialog.findViewById(R.id.version)!!
        textLog = versionDialog.findViewById(R.id.log)!!
        textVersion.text = "v${version.VersionName}"
        textLog.text = version.ChangedLog.replace("\\n", "\n")
        btnUpdate.setOnClickListener {
            btnUpdate.visibility = View.GONE
            textProgress.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            var fileName = "ixueto_v${version.VersionName}.apk"
            if (isExternalStorageWritable()) {
                initDownload(version.DownloadUrl, fileName)
            } else {
                Snackbar.make(binding!!.root!!, "无文件访问权限", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun initDownload(url: String, fileName: String) {
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(binding!!.root!!, "更新包下载失败，请检查网络", Snackbar.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body
                val inputStream: InputStream = body!!.byteStream()
                saveFile(
                    inputStream,
                    fileName!!,
                    body!!.contentLength()
                )
            }
        })
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun saveFile(inputStream: InputStream, filename: String, fileLength: Long) {
        var count: Long = 0
        try {
            val appSpecificExternalDir =
                File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)
            val outputStream = FileOutputStream(appSpecificExternalDir)
            var length = -1
            val bytes = ByteArray(1024 * 10)
            this.runOnUiThread {
                progressBar.max = fileLength.toInt()
            }
            while (inputStream.read(bytes).also { length = it } != -1) {
                outputStream.write(bytes, 0, length)
                count += length.toLong()
                val finalCount = count
                this.runOnUiThread {
                    progressBar.setProgressCompat(finalCount.toInt(), true)
                    textProgress.text = "${100 * finalCount / fileLength}%"
                }
            }
            inputStream.close()
            outputStream.close()
            this.runOnUiThread {
                //versionDialog.dismiss()
                installFile = appSpecificExternalDir
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    installApk()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        if (!packageManager.canRequestPackageInstalls()) {
//                            startInstallPermissionSettingActivity()
//                        } else {
//                            installApk()
//                        }
                        installApk()
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
                            )
                        }
                        installApk()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startInstallPermissionSettingActivity() {
        var intent = Intent()
        val packageURI = Uri.parse("package:${this.packageName}")
        intent.data = packageURI
        if (Build.VERSION.SDK_INT >= 26) {
            intent.action = Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
        } else {
            intent.action = Settings.ACTION_SECURITY_SETTINGS
        }
        launcher.launch(intent)
        installApk()
    }

    private fun installApk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val apkUri: Uri = FileProvider.getUriForFile(
                this,
                "com.xktech.ixueto.fileprovider", installFile
            )
            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.setDataAndType(apkUri, "application/vnd.android.package-archive")
            startActivity(install)
        } else {
            val install = Intent(Intent.ACTION_VIEW)
            install.setDataAndType(
                Uri.fromFile(installFile),
                "application/vnd.android.package-archive"
            )
            install.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(install)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    @AfterPermissionGranted(1)
    private fun methodRequiresPermission() {
        if (!EasyPermissions.hasPermissions(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission),
                1,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    //    private var fontScale = 1f
//
//    override fun getResources(): Resources? {
//        val resources: Resources = super.getResources()
//        return DisplayUtil.getResources(this, resources, fontScale)
//    }
//
//    override fun attachBaseContext(newBase: Context?) {
//        super.attachBaseContext(DisplayUtil.attachBaseContext(newBase, fontScale))
//    }
//
//    @JvmName("setFontScale1")
//    fun setFontScale(fontScale: Float) {
//        this.fontScale = fontScale
//        DisplayUtil.recreate(this)
//    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        if (resources.displayMetrics.scaledDensity != scaledDensity) {
            resources.displayMetrics.scaledDensity = scaledDensity
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun setDensity() {
        val systemMetrics = getSystemMetrics()
        var scale = 1.0f // 根据需求定义系数
        if (systemMetrics.density - 2.625 > 0.05) {
            scale = (2.625 / systemMetrics.density).toFloat()
        }
        with(resources!!.displayMetrics) {
            density = systemMetrics.density * scale
            scaledDensity = systemMetrics.density * scale
            densityDpi = (systemMetrics.densityDpi * scale).toInt()
        }
        this@MainActivity.scaledDensity = systemMetrics.density * scale
    }

    private fun getSystemMetrics(): DisplayMetrics {
        return applicationContext.resources.displayMetrics
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}