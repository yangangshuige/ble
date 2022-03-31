package com.yg.ble.callback

import com.yg.ble.exception.BleException

abstract class BleNotifyCallback : BleBaseCallback() {
    abstract fun onNotifySuccess()

    abstract fun onNotifyFailure(exception: BleException?)

    abstract fun onCharacteristicChanged(data: ByteArray?)
}