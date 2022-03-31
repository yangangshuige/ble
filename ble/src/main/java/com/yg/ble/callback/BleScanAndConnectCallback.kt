package com.yg.ble.callback

import com.yg.ble.data.BleDevice

abstract class BleScanAndConnectCallback : BleGattCallback(), BleScanPresenterImp {
    abstract fun onScanFinished(scanResult: BleDevice?)

    fun onLeScan(bleDevice: BleDevice?) {}
}