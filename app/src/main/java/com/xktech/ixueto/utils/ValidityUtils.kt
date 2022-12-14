package com.xktech.ixueto.utils

import java.util.regex.Pattern

object ValidityUtils {
    fun isPhone(text: String): Boolean {
        return Pattern.matches(
            "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}\$",
            text
        )
    }
}