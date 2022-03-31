package com.yg.ble.callback

import com.yg.ble.data.BleDevice

interface BleScanPresenterImp {
    fun onScanStarted(success: Boolean)

    fun onScanning(bleDevice: BleDevice)
}