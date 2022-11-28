package com.xktech.ixueto.model

data class Version(
    val VersionCode: Int,
    val VersionName: String,
    val ChangedLog: String,
    val PublishedTime: String,
    val DownloadUrl: String
)