package com.example.test.utils

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.didi.bike.applicationholder.AppContextHolder
import java.io.File
import java.security.MessageDigest
import kotlin.experimental.and

object PhoneUtils {
    fun bluetoothDeviceId(): String {
        // 这里首先使用Android id，如果Android id获取失败，再使用系统其它方案实现
        val context = AppContextHolder.applicationContext<Application>()
        val androidId = Settings.System.getString(context.contentResolver,
            Settings.Secure.ANDROID_ID)

        if (!TextUtils.isEmpty(androidId)) {
            return androidId
        }

        val systemImei = getIMEI(context)
        return strToMD5(systemImei)
    }

    fun getIMEI(context: Context): String? {
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var strImei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.imei
        } else {
            manager.deviceId
        }
        if (strImei == null || strImei.isEmpty() || strImei == "null") {
            strImei =
                "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10
        }
        val last: String = getLastModifiedMD5Str()
        return strImei + last
    }
    private fun getLastModifiedMD5Str(): String {
        val path = "/system/build.prop"
        val f = File(path)
        val modified = f.lastModified()
        return strToMD5(modified.toString())
    }
    private fun strToMD5(str: String?): String {
        if (str == null) {
            return ""
        }
        var messageDigest: MessageDigest? = null
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(str.toByteArray(charset("UTF-8")))
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
        return toHexStr(messageDigest.digest())
    }

    private fun toHexStr(bytes: ByteArray): String {
        val md5sb = StringBuilder()
        for (b in bytes) {
            // details refer to:
            // 1. http://www.avajava.com/tutorials/lessons/how-do-i-generate-an-md5-digest-for-a-string.html
            // 2. http://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal
            md5sb.append(String.format("%02x", b and 0xff.toByte()))
        }
        return md5sb.toString()
    }
}