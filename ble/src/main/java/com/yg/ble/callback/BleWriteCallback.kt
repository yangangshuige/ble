package com.yg.ble.callback

import com.yg.ble.exception.BleException

abstract class BleWriteCallback : BleBaseCallback() {
    abstract fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray)

    abstract fun onWriteFailure(exception: BleException)
}