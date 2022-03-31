package com.yg.ble.callback

import com.yg.ble.data.BleDevice

abstract class BleScanCallback : BleScanPresenterImp {
    abstract fun onScanFinished(scanResultList: List<BleDevice>)

    open fun onLeScan(bleDevice: BleDevice) {}
}