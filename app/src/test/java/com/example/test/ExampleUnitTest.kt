package com.example.test

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

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
    fun myTest() {
        println(priceDes(76500))
    }

    fun is16YearOld(birth: String): Boolean {
        var timestamp = SimpleDateFormat("yyyy-MM-dd").parse(birth).time
        var calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] - 16)
        return calendar.timeInMillis > timestamp
    }

    fun durationDes(duration: Int): String {
        if (duration == 180) return "半年"
        val year = duration / 360
        val month = (duration - year * 360) / 30
        val day = (duration - year * 360 - month * 30)
        var des = ""
        if (year > 0)
            des = "$year" + "年"
        if (month > 0)
            des = des + "$month" + "个月"
        if (day > 0)
            des = des + "$day" + "天"
        return des
    }

    fun priceDes(price: Int): String {

        return price.div(100f).toString()
    }

    @Test
    fun myTest2() {
        val length = 2
        val data = byteArrayOf(11, 12, 13, 14, 15, 16)
        val fileSize = data.size
        val writeDate = ByteArray(fileSize - length)
        System.arraycopy(data, length, writeDate, 0, data.size - length)
        for (i in writeDate.indices) {
            println("writeDate========" + writeDate[i])
        }
    }

    @Test
    fun myTest3() {
        val boolean = AtomicBoolean(true)
        boolean.compareAndSet(false, true)
        println("compareAndSet========" +  boolean.compareAndSet(false, true))
        println("boolean========" + boolean.get())
    }
    @Test
    fun myTest4() {
      val string = "1,2,3,4,5"
        val list = string.split(",".toRegex()) as ArrayList<String>
        for( item in list){
            println(item)
        }
    }
}