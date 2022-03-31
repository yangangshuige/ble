package com.yg.ble.utils

import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.experimental.and

object HexUtil {
    private val DIGITS_LOWER = charArrayOf('0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    private val DIGITS_UPPER = charArrayOf('0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    fun encodeHex(data: ByteArray?): CharArray? {
        return encodeHex(data, true)
    }

    fun encodeHex(data: ByteArray?, toLowerCase: Boolean): CharArray? {
        return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    private fun encodeHex(data: ByteArray?, toDigits: CharArray): CharArray? {
        if (data == null) return null
        val l = data.size
        val out = CharArray(l shl 1)
        var i = 0
        var j = 0
        while (i < l) {
            out[j++] = toDigits[0xF0 and data[i].toInt() ushr 4]
            out[j++] = toDigits[0x0F and data[i].toInt()]
            i++
        }
        return out
    }

    fun encodeHexStr(data: ByteArray?): String? {
        return encodeHexStr(data, true)
    }

    fun encodeHexStr(data: ByteArray?, toLowerCase: Boolean): String? {
        return encodeHexStr(data,
            if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    private fun encodeHexStr(data: ByteArray?, toDigits: CharArray): String? {
        return if (data == null) "" else encodeHex(data, toDigits)?.let { String(it) }
    }

    fun formatHexString(data: ByteArray?): String? {
        return formatHexString(data, false)
    }

    fun formatHexString(data: ByteArray?, addSpace: Boolean): String? {
        if (data == null || data.isEmpty()) return null
        val sb = StringBuilder()
        for (i in data.indices) {
            var hex = Integer.toHexString((data[i] and 0xFF.toByte()).toInt())
            if (hex.length == 1) {
                hex = "0$hex"
            }
            sb.append(hex)
            if (addSpace) sb.append(" ")
        }
        return sb.toString().trim { it <= ' ' }
    }

    fun isMac(mac: String): Boolean {
        // 验证规则
        val regEx = "^([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})\$"
        // 编译正则表达式  忽略大小写的写法
        val pattern: Pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(mac)
        // 字符串是否与正则表达式相匹配
        return matcher.matches()
    }
}