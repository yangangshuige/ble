package com.yg.ble

import com.yg.ble.utils.HexUtil
import org.junit.Test

import org.junit.Assert.*
import kotlin.experimental.and

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    @Test
    fun mTest() {
        val bytes = byteArrayOf(10, 2, 15, 11)
        println(HexUtil.encodeHexStr(bytes))
        println(HexUtil.formatHexString(bytes))
        println(bytesToHex(bytes))
    }
    fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = (bytes[j] and 0xFF.toByte()).toInt()
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}