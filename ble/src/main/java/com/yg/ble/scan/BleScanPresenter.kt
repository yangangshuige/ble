package com.yg.ble.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import com.yg.ble.callback.BleScanPresenterImp
import com.yg.ble.data.BleDevice
import com.yg.ble.data.BleMsg
import com.yg.ble.utils.BleLog
import com.yg.ble.utils.HexUtil
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

abstract class BleScanPresenter : BluetoothAdapter.LeScanCallback {
    private var mDeviceNames = arrayListOf<String>()
    private var mDeviceMac: String = ""
    private var mFuzzy = false
    private var mNeedConnect = false
    private var mScanTimeout: Long = 0
    var mBleScanPresenterImp: BleScanPresenterImp?=null

    private val mBleDeviceList = ArrayList<BleDevice>()

    private val mMainHandler = Handler(Looper.getMainLooper())
    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null
    private var mHandling = false

    private class ScanHandler constructor(
        looper: Looper,
        bleScanPresenter: BleScanPresenter,
    ) : Handler(looper) {
        private val mBleScanPresenter: WeakReference<BleScanPresenter> =
            WeakReference<BleScanPresenter>(bleScanPresenter)

        override fun handleMessage(msg: Message) {
            val bleScanPresenter: BleScanPresenter? = mBleScanPresenter.get()
            if (bleScanPresenter != null) {
                if (msg.what == BleMsg.MSG_SCAN_DEVICE) {
                    val bleDevice: BleDevice = msg.obj as BleDevice
                    if (bleDevice != null) {
                        bleScanPresenter.handleResult(bleDevice)
                    }
                }
            }
        }

    }

    private fun handleResult(bleDevice: BleDevice) {
        mMainHandler.post { onLeScan(bleDevice) }
        checkDevice(bleDevice)
    }

    fun prepare(
        names: ArrayList<String>, mac: String, fuzzy: Boolean, needConnect: Boolean,
        timeOut: Long, bleScanPresenterImp: BleScanPresenterImp,
    ) {
        mDeviceNames = names
        mDeviceMac = mac
        mFuzzy = fuzzy
        mNeedConnect = needConnect
        mScanTimeout = timeOut
        mBleScanPresenterImp = bleScanPresenterImp
        mHandlerThread = HandlerThread(BleScanPresenter::class.java.simpleName)
        mHandlerThread?.start()
        mHandler = mHandlerThread?.looper?.let { ScanHandler(it, this) }
        mHandling = true
    }

    fun ismNeedConnect(): Boolean {
        return mNeedConnect
    }

    override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
        if (device == null) return
        if (!mHandling) return
        val message = mHandler?.obtainMessage()
        message?.what = BleMsg.MSG_SCAN_DEVICE
        message?.obj =
            BleDevice(device, rssi, scanRecord ?: byteArrayOf(), System.currentTimeMillis())
        mHandler?.sendMessage(message)
    }

    private fun checkDevice(bleDevice: BleDevice) {
        if (TextUtils.isEmpty(mDeviceMac) && mDeviceNames.isEmpty()) {
            correctDeviceAndNextStep(bleDevice)
            return
        }
        if (!TextUtils.isEmpty(mDeviceMac)) {
            if (!mDeviceMac.equals(bleDevice.getMac(), ignoreCase = true)) return
        }
        if (mDeviceNames != null && mDeviceNames.size > 0) {
            val equal = AtomicBoolean(false)
            for (name in mDeviceNames) {
                var remoteName: String = bleDevice.getName()
                if (remoteName == null) remoteName = ""
                BleLog.d("remoteName========$remoteName")
                if (if (mFuzzy) remoteName.contains(name, true) else remoteName == name) {
                    equal.set(true)
                }
            }
            if (!equal.get()) {
                return
            }
        }
        correctDeviceAndNextStep(bleDevice)
    }

    private fun correctDeviceAndNextStep(bleDevice: BleDevice) {
        if (mNeedConnect) {
            BleLog.i("devices detected  ------"
                    + "  name:" + bleDevice.getName()
                    + "  mac:" + bleDevice.getMac()
                    + "  Rssi:" + bleDevice.getRssi()
                    + "  scanRecord:" + HexUtil.formatHexString(bleDevice.getScanRecord()))
            mBleDeviceList.add(bleDevice)
            mMainHandler.post { BleScanner.instance.stopLeScan() }
        } else {
            val hasFound = AtomicBoolean(false)
            for (result in mBleDeviceList) {
                if (result.getDevice() == bleDevice.getDevice()) {
                    hasFound.set(true)
                }
            }
            if (!hasFound.get()) {
                BleLog.i("device detected  ------"
                        + "  name: " + bleDevice.getName()
                        + "  mac: " + bleDevice.getMac()
                        + "  Rssi: " + bleDevice.getRssi()
                        + "  scanRecord: " + HexUtil.formatHexString(bleDevice.getScanRecord(),
                    true))
                mBleDeviceList.add(bleDevice)
                mMainHandler.post { onScanning(bleDevice) }
            }
        }
    }

    fun notifyScanStarted(success: Boolean) {
        mBleDeviceList.clear()
        removeHandlerMsg()
        if (success && mScanTimeout > 0) {
            mMainHandler.postDelayed({ BleScanner.instance.stopLeScan() },
                mScanTimeout)
        }
        mMainHandler.post { onScanStarted(success) }
    }

    fun notifyScanStopped() {
        mHandling = false
        mHandlerThread?.quit()
        removeHandlerMsg()
        mMainHandler.post { onScanFinished(mBleDeviceList) }
    }

    fun removeHandlerMsg() {
        mMainHandler.removeCallbacksAndMessages(null)
        mHandler?.removeCallbacksAndMessages(null)
    }

    abstract fun onScanStarted(success: Boolean)

    abstract fun onLeScan(bleDevice: BleDevice)

    abstract fun onScanning(bleDevice: BleDevice)

    abstract fun onScanFinished(bleDeviceList: List<BleDevice>)
}