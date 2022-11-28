package com.xktech.ixueto.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Cookie(val url: String, val value: String) : Parcelable