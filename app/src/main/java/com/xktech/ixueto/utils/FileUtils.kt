package com.xktech.ixueto.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import androidx.annotation.RequiresApi
import java.io.*

object FileUtils {
    fun getFileAbsolutePath(context: Context?, imageUri: Uri?): String? {
        if (context == null || imageUri == null) {
            return null
        }

        //4.4以下的版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return getRealFilePath(context, imageUri)
        }

        //大于4.4，小于10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && DocumentsContract.isDocumentUri(
                context,
                imageUri
            )
        ) {
            if (isExternalStorageDocument(imageUri)) {
                val docId: String = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(imageUri)) {
                val id: String = DocumentsContract.getDocumentId(imageUri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(imageUri)) {
                val docId: String = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return contentUri?.let { getDataColumn(context, it, selection, selectionArgs) }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return uriToFileApiQ(context, imageUri)
        } else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(imageUri)) {
                return imageUri.lastPathSegment
            }
            return if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.N) {
                getFilePathFromUri(context, imageUri)
            } else {
                getDataColumn(context, imageUri, null, null)
            }
        } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
            return imageUri.path
        }
        return null
    }

    private fun getRealFilePath(context: Context, uri: Uri?): String? {
        if (null == uri) {
            return null
        }
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) {
            data = uri.path
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Android 10 以上适配
     * @param context
     * @param uri
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun uriToFileApiQ(context: Context, uri: Uri): String? {
        var file: File? = null
        //android10以上转换
        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            file = File(uri.path)
        } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //把文件复制到沙盒目录
            val contentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                if (cursor.moveToFirst()) {
                    val displayName: String =
                        cursor.getString(cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    try {
                        val `is`: InputStream? = contentResolver.openInputStream(uri)
                        val file1 =
                            File(context.externalCacheDir!!.absolutePath + "/" + System.currentTimeMillis())
                        if (!file1.exists()) {
                            file1.mkdir()
                        }
                        val cache = File(file1.path, displayName)
                        val fos = FileOutputStream(cache)
                        if (`is` != null) {
                            android.os.FileUtils.copy(`is`, fos)
                        }
                        file = cache
                        fos.close()
                        `is`?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return file?.absolutePath
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val realFilePath = getRealFilePath(context, uri)
        //防止获取不到真实的地址，因此这里需要进行判断
        if (!TextUtils.isEmpty(realFilePath)) {
            return realFilePath
        }
        val filesDir = context.applicationContext.filesDir
        val fileName: String? = getFileName(uri)
        if (!TextUtils.isEmpty(fileName)) {
            val copyFile1 = File(filesDir.toString() + File.separator + fileName)
            copyFile(context, uri, copyFile1)
            return copyFile1.absolutePath
        }
        return null
    }

    private fun getFileName(uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        var fileName: String? = null
        val path = uri.path
        val cut = path!!.lastIndexOf('/')
        if (cut != -1) {
            fileName = path.substring(cut + 1)
        }
        return fileName
    }

    private fun copyFile(context: Context, srcUri: Uri, dstFile: File) {
        try {
            val inputStream = context.contentResolver.openInputStream(srcUri) ?: return
            val outputStream: OutputStream = FileOutputStream(dstFile)
            copyStream(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream): Int {
        val bufferSize = 1024 * 2
        val buffer = ByteArray(bufferSize)
        val `in` = BufferedInputStream(input, bufferSize)
        val out = BufferedOutputStream(output, bufferSize)
        var count = 0
        var n = 0
        try {
            while (`in`.read(buffer, 0, bufferSize).also { n = it } != -1) {
                out.write(buffer, 0, n)
                count += n
            }
            out.flush()
        } catch (e: java.lang.Exception) {
        } finally {
            try {
                out.close()
                `in`.close()
            } catch (e: java.lang.Exception) {
            }
        }
        return count
    }

}