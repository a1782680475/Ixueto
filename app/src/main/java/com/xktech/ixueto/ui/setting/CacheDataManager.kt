package com.xktech.ixueto.ui.setting

import android.content.Context
import android.os.Environment
import java.io.File
import java.math.BigDecimal

object CacheDataManager {
    /**
     * 获取缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getTotalCacheSize(context: Context): String? {
        var cacheSize = getFolderSize(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            cacheSize += getFolderSize(context.externalCacheDir)
            cacheSize += getFolderSize(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
        }
        return getFormatSize(cacheSize.toDouble())
    }

    /**
     * 清除缓存
     *
     * @param context
     */
    fun clearAllCache(context: Context) {
        deleteDir(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            deleteDir(context.externalCacheDir)
            clearInstallFile(context)
        }
        clearAuthFile(context)
        cleanDatabases(context)
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
    }

    @Throws(Exception::class)
    fun getFolderSize(file: File?): Long {
        var size: Long = 0
        try {
            val fileList = file!!.listFiles()
            for (i in fileList.indices) {

                // 如果下面还有文件
                size = if (fileList[i].isDirectory) {
                    size + getFolderSize(fileList[i])
                } else {
                    size + fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     *
     * @param size
     */
    private fun getFormatSize(size: Double): String? {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return size.toString() + "B"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(kiloByte.toString())
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(megaByte.toString())
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(gigaByte.toString())
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "G"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "T"
    }

    /**
     * 删除应用安装目录
     *
     * @param context
     */
    private fun clearInstallFile(context: Context) {
        var path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path
        deleteDir(File(path))
    }

    /**
     * 清除本应用所有数据库
     *
     * @param context
     */
    private fun cleanDatabases(context: Context) {
        deleteDir(
            File(
                "/data/data/${context.packageName}/databases"
            )
        )
    }

    private fun clearAuthFile(context: Context){
        var path = "${context.cacheDir}/auth/image"
        deleteDir(File(path))
    }
}