package com.yg.ble.callback

import com.yg.ble.exception.BleException

abstract class BleMtuChangedCallback : BleBaseCallback() {
    abstract fun onSetMTUFailure(exception: BleException?)

    abstract fun onMtuChanged(mtu: Int)
}