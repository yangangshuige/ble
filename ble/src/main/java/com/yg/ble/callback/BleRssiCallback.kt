package com.yg.ble.callback

import com.yg.ble.exception.BleException

abstract class BleRssiCallback : BleBaseCallback() {
    abstract fun onRssiFailure(exception: BleException?)

    abstract fun onRssiSuccess(rssi: Int)
}