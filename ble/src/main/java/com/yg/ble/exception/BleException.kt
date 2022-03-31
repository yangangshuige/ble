package com.yg.ble.exception

import java.io.Serializable

abstract class BleException(code: Int, description: String) : Serializable {
    private val code = code
     val description = description

    companion object {
        const val ERROR_CODE_TIMEOUT = 100
        const val ERROR_CODE_GATT = 101
        const val ERROR_CODE_OTHER = 102
    }
}