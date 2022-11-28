package com.xktech.ixueto.utils

object Encrypt {
    fun customEncrypt(source: String): String {
        var cR1 = 81247
        var cR2 = 29885
        var i = 0
        var result = ""
        var strTmp = ""
        var key = 512;
        var charArray = source.toCharArray()
        while (i < source.length) {
            var cTemp = charArray[i].code.xor(key.shr(8))
            key = ((cTemp + key) * cR1 + cR2).and(65535)
            result += cTemp.toChar()
            i++
        }
        strTmp = result;
        result = "";
        i = 0
        while (i < source.length) {
            var j = strTmp.toCharArray()[i].code
            result =
                result + (65 + (j / 26)).toChar() + (65 + (j % 26)).toChar()
            i++
        }
        return result;
    }
}