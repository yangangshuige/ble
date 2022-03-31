package com.yg.ble.utils

import com.yg.ble.bluetooth.BleBluetooth
import kotlin.math.ceil

class BleLruHashMap<K, V>(saveSize: Int) :
    LinkedHashMap<K, V>(ceil(saveSize / 0.75).toInt() + 1, 0.75f, true) {
    private val maxSize = saveSize

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        if (size > maxSize && eldest?.value is BleBluetooth) {
            (eldest?.value as BleBluetooth).disconnect()
        }
        return size > maxSize
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for ((key, value) in entries) {
            sb.append(String.format("%s:%s ", key, value))
        }
        return sb.toString()
    }
}