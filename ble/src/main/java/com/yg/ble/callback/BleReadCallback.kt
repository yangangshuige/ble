package com.yg.ble.callback

import com.yg.ble.exception.BleException

abstract class BleReadCallback : BleBaseCallback() {
    abstract fun onReadSuccess(data: ByteArray?)

    abstract fun onReadFailure(exception: BleException?)
}