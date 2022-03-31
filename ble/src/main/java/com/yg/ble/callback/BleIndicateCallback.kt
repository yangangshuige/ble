package com.yg.ble.callback

import com.yg.ble.exception.BleException

abstract class BleIndicateCallback : BleBaseCallback() {
    abstract fun onIndicateSuccess()

    abstract fun onIndicateFailure(exception: BleException?)

    abstract fun onCharacteristicChanged(data: ByteArray?)
}