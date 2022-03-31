package com.yg.ble.scan

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.yg.ble.BleManager
import com.yg.ble.callback.BleScanAndConnectCallback
import com.yg.ble.callback.BleScanCallback
import com.yg.ble.callback.BleScanPresenterImp
import com.yg.ble.data.BleDevice
import com.yg.ble.data.BleScanState
import com.yg.ble.utils.BleLog
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("MissingPermission")
class BleScanner {
    var mBleScanState = BleScanState.STATE_IDLE
    private val mBleScanPresenter: BleScanPresenter =
        object : BleScanPresenter() {
            override fun onScanStarted(success: Boolean) {
                mBleScanPresenterImp?.onScanStarted(success)
            }

            override fun onLeScan(bleDevice: BleDevice) {
                if (this.ismNeedConnect()) {
                    (mBleScanPresenterImp as BleScanAndConnectCallback).onLeScan(bleDevice)
                } else {
                    (mBleScanPresenterImp as BleScanCallback).onLeScan(bleDevice)
                }
            }

            override fun onScanning(result: BleDevice) {
                mBleScanPresenterImp?.onScanning(result)
            }

            override fun onScanFinished(bleDeviceList: List<BleDevice>) {
                if (this.ismNeedConnect()) {
                    if (bleDeviceList.isNullOrEmpty()) {
                        (mBleScanPresenterImp as BleScanAndConnectCallback).onScanFinished(null)
                    } else {
                        (mBleScanPresenterImp as BleScanAndConnectCallback).onScanFinished(
                            bleDeviceList[0])
                        val list: List<BleDevice> = bleDeviceList
                        Handler(Looper.getMainLooper()).postDelayed({
                            BleManager.instance.connect(list[0],
                                mBleScanPresenterImp as BleScanAndConnectCallback)
                        }, 100)
                    }
                } else {
                    (mBleScanPresenterImp as BleScanCallback?)?.onScanFinished(bleDeviceList)
                }
            }
        }

    fun scan(
        serviceUuids: ArrayList<UUID>, names: ArrayList<String>, mac: String, fuzzy: Boolean,
        timeOut: Long, callback: BleScanCallback,
    ) {
        startLeScan(serviceUuids, names, mac, fuzzy, false, timeOut, callback)
    }

    fun scanAndConnect(
        serviceUuids: ArrayList<UUID>, names: ArrayList<String>, mac: String, fuzzy: Boolean,
        timeOut: Long, callback: BleScanAndConnectCallback,
    ) {
        startLeScan(serviceUuids, names, mac, fuzzy, true, timeOut, callback)
    }

    @Synchronized
    private fun startLeScan(
        serviceUuids: ArrayList<UUID>, names: ArrayList<String>, mac: String, fuzzy: Boolean,
        needConnect: Boolean, timeOut: Long, imp: BleScanPresenterImp,
    ) {
        if (mBleScanState != BleScanState.STATE_IDLE) {
            BleLog.w("scan action already exists, complete the previous scan action first")
            imp.onScanStarted(false)
            return
        }
        mBleScanPresenter.prepare(names, mac, fuzzy, needConnect, timeOut, imp)
        val success: Boolean = if (serviceUuids.isNullOrEmpty()) {
            BleManager.instance.bluetoothAdapter?.startLeScan(mBleScanPresenter) == true
        } else {
            BleManager.instance.bluetoothAdapter?.startLeScan(serviceUuids.toTypedArray(),
                mBleScanPresenter) == true
        }
        mBleScanState =
            if (success) BleScanState.STATE_SCANNING else BleScanState.STATE_IDLE
        mBleScanPresenter.notifyScanStarted(success)
    }

    @Synchronized
    fun stopLeScan() {
        BleManager.instance.bluetoothAdapter?.stopLeScan(mBleScanPresenter)
        mBleScanState = BleScanState.STATE_IDLE
        mBleScanPresenter.notifyScanStopped()
    }

    companion object {
        val instance: BleScanner by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BleScanner()
        }
    }
}